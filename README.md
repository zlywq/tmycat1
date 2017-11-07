分布式事务的一种实现方式————状态流转

[TOC]

关于分布式事务，参考了网上提到的一些办法，比如利用消息队列实现分布式事务，补偿事务，TCC，最大努力送达，等等。这里给出自己的一些理解和实现。可以称之为状态流转的实现。

## 一些要点
- 大事务拆分成多个小事务，每个小事务都是单机上的事务
- 要支持幂等，即每个小事务多次执行时结果要相同。
 - 如何达到幂等，一般是加一个状态，如一个带有状态字段的行，在写入业务数据时同时修改状态为“已做”，“已撤销”之类的。下次再做时，先检查状态的值，如果存在且为“已做”，则不用再做了；如果不存在，说明是“未做”，这时需要写入业务数据。
- 对应一个大事务的多个小事务如何保证全部成功或者全部失败。
 - 先分析如何保证全部成功，就是多做几次，直到全部成功。由于有幂等的保证，过程中某些小事务多做了几次，是不影响数据的正确性的。
 - 同理可以分析全部失败即需要回滚的情况，也是在幂等保证的前提下多做几次，直到全部回滚完成。
 - 如何保证多做几次，办法应该很多，一种方法是在第一个小事务执行前先发一个延时消息，这个消息中包含预先生成的对应整个大事务流程的id。这样就算当前事务流程中在某个步骤失败，以后也有再次检查执行的机会。  
 - 其他的一些技巧。使用唯一索引保证操作的唯一性以及防并发操作。使用CAS(compare and set)来防并发修改操作，如专设一个rowver字段。

***

## 分析一个简单的例子，转账业务。  


- 相关的表如下  
 - Account(userId,amount,rowver)  
 - AccountTransferState(acsId,idForPart,state,fromUserId,toUserId,amount,rowver)  
     - 注意AccountChangeState有2份记录，只是idForPart不同（注意其他相同，如acsId等相同），分别为fromUserId和toUserId。这样与Account是一一对应的，并且分区方式相同，这样方便事务处理。  
  
### 先分析流程中的正常情况。  
- 0,预先生成acsId，并发送消息到队列，消息内容包括AccountTransferState的几乎所有字段 以及 msgType=checkTransfer(在别的情况下还可以msgType=newTransfer，相当于异步进行转账操作)等。  
 - 利用消息队列是自动处理，还可以人工干预。比如，用户（这里指fromUser）可以查询一个AccountTransferState的列表，按时间从晚到早排序。如果哪条记录未完成，可以点按钮触发操作。  
 - 综上2种情况，在预先生成acsId后，都有办法拿到这个acsId以及其他相关数据（如fromUserId等等）。  
- 1,在单机事务中,先检查fromAccount的amount，  
 - 如果不够，则直接返回，整个大事务以fail结束。  
 - 如果够，减少fromAccount 并 新建AccountTransferState(acsId为预先生成,idForPart=fromUserId,state=didOut)。  
- 2,在单机事务中 增加toAccount 并 新建AccountChangeState(idForPart=toUserId,state=finish)  
- 3,在单机事务中 修改idForPart=fromUserId的AccountTransferState，使state=finish。  

### 再分析流程中的异常情况。  
&emsp;&emsp;在1，2，3中每一步都可能出异常。（第0步出异常，处理很简单，参考下面的1B步骤即可）    
&emsp;&emsp;由于第0步存在，总有办法拿到具体的id和必要数据。从而可以分析检查重做的过程，如下。  

- 1B,在单机事务中,使用acsId和idForPart=fromUserId查询AccountTransferState记录，  
 - 如果不存在，说明第1步的事务由于各种原因失败，从而是整个流程失败，不用管了。（另外也可以考虑再次转账，不过在这里暂不分析，以避免引入一些不必要的复杂性）    
 - 如果存在，看AccountTransferState记录内容，  
     - 如果state=finish，则说明是在第3步事务完成后出了异常，此时整个流程已经成功完成，不用在这里做什么。    
     - 如果state=didOut，说明第1步已经完成，由下一步来处理，见2B.    
     - 如果state=其他，按业务逻辑分析不应该出现，给开发人员报bug。    
- 2B, 在单机事务中,使用acsId和idForPart=toUserId查询AccountTransferState记录，    
 - 如果不存在，说明第2步尚未开始或未能成功开始，只需简单做前面的2、3步中的内容    
 - 如果存在，看AccountTransferState记录内容，    
     - 如果state=finish，说明第2步已经成功完成，只需简单做前面的第3步中的内容  
     - 如果state=其他，按业务逻辑分析不应该出现，给开发人员报bug。  
- 3B,同前面的第3步  
 - 这步比较简单。根据上面的分析，如果能够到这步，只有当前面2步都成功时才到。所以无需判断什么条件，只需简单执行第3步的内容即可。  




***
## 分析一个比较复杂的例子，使用积分和余额付款，积分和余额在不同的库中。
由于在不同的库中，还可以把一个小事务抽象成一个远程调用。  

	相关的表如下  
	这里AccountTransferRequest与4个Account一般是5片。  
	AccountTransferRequest(atrId,fromUserId,toUserId,state,crtTime,detailJson,shuoming,rowver)//detailJson 包括 多条记录，每条记录有 AccountType, amount  
	AccountYuE(userId,amount,rowver)  
	AccountJiFen(userId,amount,rowver)  
	AccountYuEChangeState  (userIdForPart,atrId,state,rowver, amount)  
	AccountJiFenChangeState(userIdForPart,atrId,state,rowver, amount)  
		注意AccountXyzChangeState有2份记录，分别与2个AccountXyz对应，且分区方式相同，这样保证单机事务
	
    初次执行所有事务时(第一次也称创建阶段)
	1,建立转账请求 AccountTransferRequest(state=Init)
		在事务前预先生成atrId,(还考虑先保存到导致这个动作的根动作上，这样便于追查状态)。
		第一次执行暂且不用分布式锁，因为atrId是新生成的，没有别的地方争用。除非是锁根动作的id。
		发送延时消息到队列(1minute,msgType=checkTransfer)。消息内容包括AccountTransferRequest的几乎所有字段，重点的有atrId,fromUserId,msgType=checkTransfer(当异步进行转账操作，还可以msgType=newTransfer)。
		另一方面，用户（这里是fromUser），可以by fromUserId查询一个AccountTransferRequest的列表，按时间从晚到早排序。如果哪条记录未完成，可以点按钮触发操作。（或者从某个根动作得到atrId，并进一步取到数据）
		综上2种情况，都可以拿到atrId和fromUserId。
		另外，toUser暂时认为没有查询AccountTransferRequest的列表的必要。
			一方面，没转账成功，toUser不必知道这个请求状态的中间过程(毕竟现在的电商网站也没有提供这种查询)。
			另一方面，还可以查AccountChangeState作为列表，至于join问题，可以冗余几个字段或一个json字符串字段，或者等这个数据流转到异构数据库来解决查询问题。
	2,处理 AccountJiFen 的转出部分逻辑
		先检查fromAccountJiFen的amount够不够，
			够 ，则 减少fromAccountJiFen 并 新建AccountJiFenChangeState(fromUserId,atrId,state=didOut,atrDetailJson~)
			不够，则 不写fromAccountJiFen和AccountJiFenChangeState的数据。
				此时需要回滚，先发送延时消息到队列，消息内容包括AccountTransferRequest的几乎所有字段，重点的有atrId,msgType=toRollback。
					如果或就算在发送消息前出异常停掉。下次通过消息进入检查处理流程，会重试步骤2，可以走够与不够2个分支，但不会导致数据不一致等错误。
				然后修改AccountTransferRequest的state=toRollback，并修改shuoming字段为失败原因，这里是积分不足。然后返回失败原因提示用户或者发消息通知用户。
				然后执行B处的回滚逻辑，而不是执行下面的逻辑。考虑到通用性，B处的回滚逻辑包括多处数据的处理。
				注意这里由于有分支路径，需要避免并发。因为不排除在某些情况下，相同的两个消息由2个线程并发处理，而且2个线程走的分支不一样，比如先执行的发现amount不够需要回滚，而后执行的发现amount够了（比如之前amount被另一个线程增加了）而继续执行正常分支。这需要一把分布式锁作用在大事务上来避免并发。
	3,处理 AccountYuE 的转出部分逻辑
		先检查fromAccountYuE的amount够不够，
			够 ，则 减少fromAccountYuE 并 新建AccountYuEChangeState(fromUserId,atrId,state=didOut,atrDetailJson~)
			不够，则 不写fromAccountYuE和AccountYuEChangeState的数据。
				而是修改AccountTransferRequest的state=toRollback，并修改shuoming字段为失败原因，这里是余额不足。然后返回失败原因提示用户或者发消息通知用户。
				然后执行B处的回滚逻辑，而不是执行下面的逻辑。考虑到通用性，B处的回滚逻辑包括多处数据的处理。
				注意这里由于有分支路径，需要避免并发。
	4,处理 AccountJiFen 的转入部分逻辑。在事务中 增加toAccountJiFen 并 新建AccountJiFenChangeState(toUserId,atrId,state=didIn,atrDetailJsonStr~)
		此处不会出现业务逻辑问题而需要回滚，可以设想对应系统停机维护，此时出网络错不应该回滚。所以只需一直重试直到成功即可，当然重试机制需要细化考虑。
	5,处理 AccountYuE 的转入部分逻辑。在事务中 增加toAccountYuE 并 新建AccountYuEChangeState(toUserId,atrId,state=didIn,atrDetailJsonStr~)
		此处不会出现业务逻辑问题而需要回滚，可以设想对应系统停机维护，此时出网络错不应该回滚。所以只需一直重试直到成功即可，当然重试机制需要细化考虑。
	6,修改转账请求AccountTransferRequest的 state=finish
	7,有无必要修改4个AccountChangeState的state=finish，待定，暂不考虑
	----
	B 回滚逻辑
	B0 先加一个分布式锁，利用Redisson，以atrId为key。这样可以防止意外的并发执行。
	B1 先发送延时消息到队列，消息内容包括AccountTransferRequest的几乎所有字段，重点的有atrId,msgType=toRollback。以保证下面步骤出异常时，还能继续做回滚操作直到完成。
		如果AccountTransferRequest的state!=toRollback，而是state=Init，则修改AccountTransferRequest的state=toRollback。
		如果state=finish OR fail ，本来是不做任何事情，但是为何能有这条路线是很奇怪的，需要让开发调查是否存在bug。
	B2 from方的 AccountJiFen的回滚
		根据 atrId 和 fromUserId 查询 AccountJiFenChangeState 记录
			如果没查到记录，说明没做转出动作，不用回滚，即不做任何操作。
			如果查到1条记录，（不可能多条记录，否则给开发报bug）
				如果state=didOut，则需要回滚。增加fromAccountJiFen 并 修改AccountJiFenChangeState的state=didRollback 。
				如果state=didRollback，说明已经回滚了，不用再做任何操作了。
	B3 from方的 AccountYuE的回滚
		根据 atrId 和 fromUserId 查询 AccountYuEChangeState 记录
			如果没查到记录，说明没做转出动作，不用回滚，即不做任何操作。
			如果查到1条记录，（不可能多条记录，否则给开发报bug）
				如果state=didOut，则需要回滚。增加fromAccountYuE 并 修改AccountYuEChangeState的state=didRollback 。
				如果state=didRollback，说明已经回滚了，不用再做任何操作了。
	B4 to方的 AccountJiFen的回滚
		根据 atrId 和 toUserId 查询 AccountJiFenChangeState 记录
			如果没查到记录，说明没做转入动作，不用回滚，即不做任何操作。
			如果查到1条记录，（不可能多条记录，否则给开发报bug）
				如果state=didIn，则需要回滚。减少toAccountJiFen 并 修改AccountJiFenChangeState的state=didRollback 。
				如果state=didRollback，说明已经回滚了，不用再做任何操作了。
	B5 to方的 AccountYuE的回滚
		根据 atrId 和 toUserId 查询 AccountYuEChangeState 记录
			如果没查到记录，说明没做转入动作，不用回滚，即不做任何操作。
			如果查到1条记录，（不可能多条记录，否则给开发报bug）
				如果state=didIn，则需要回滚。减少toAccountYuE 并 修改AccountYuEChangeState的state=didRollback 。
				如果state=didRollback，说明已经回滚了，不用再做任何操作了。
	B6 最后修改AccountTransferRequest的state=fail
	B7 释放分布式锁
	----
	由于在1--6中每一步都可能失败或异常，研究处理失败情况。
		由于可以拿到具体的id，所以先分2种情况，一种是完全新增，此时对应上面的步骤，简单。
		一种是某个步骤失败后，通过某种方式重启这个转账流程，在下面讨论。
	
	0C,先加一个分布式锁，利用Redisson，以atrId为key。这样可以防止意外的并发执行。至于为何要避免并发在前面有地方分析过。
	1C,使用atrId查询AccountTransferRequest记录，
		如果不存在，说明第1步就没成功，可以以全新方式重做上面的1--6步。暂不考虑已经返回错误信息给用户，由用户决定，而不用做任何动作的情况。
		如果存在，看AccountTransferRequest记录内容，
			如果state=finish，此时整个流程已经成功完成，不用做什么，只需消费掉这条消息即可。
			如果state=fail，整个流程已经失败，也不用管了。
			如果state=toRollback，转入 B 回滚逻辑 的处理流程
			如果state=Init，说明至少第1步已经完成，由第2步来检查处理，见2C.
			如果state=其他，按业务逻辑分析不应该出现，给开发人员报错提醒是bug。
	2C, 使用atrId和idForPart=fromUserId查询AccountJiFenChangeState记录，
		如果不存在，说明第2步尚未开始或未能成功开始，简单做2--6步。
			这里有可能之前是第2步检查失败，但是未能修改AccountTransferRequest记录状态。注意可能出现以前检查失败，现在检查可以成功的情况。虽然单独做没有问题，但是并发做则有隐患，需要防止并发。
		如果存在，看AccountJiFenChangeState记录内容，
			如果state=didOut，说明第2步已经成功完成，由第3步来检查处理，见3C.
			如果state=didRollback,说明应该回滚了，转入 B 回滚逻辑 的处理流程
			如果state=其他，按业务逻辑分析不应该出现，给开发人员报错提醒是bug。
	3C, 使用atrId和idForPart=fromUserId查询AccountYuEChangeState记录，
		如果不存在，说明第3步尚未开始或未能成功开始，简单做3--6步。
			这里有可能之前是第3步检查失败，但是未能修改AccountTransferRequest记录状态。注意可能出现以前检查失败，现在检查可以成功的情况。虽然单独做没有问题，但是并发做则有隐患，需要防止并发。
		如果存在，看AccountYuEChangeState记录内容，
			如果state=didOut，说明第3步已经成功完成，由第4步来检查处理，见4C.
			如果state=didRollback,说明应该回滚了，转入 B 回滚逻辑 的处理流程
			如果state=其他，按业务逻辑分析不应该出现，给开发人员报错提醒是bug。
	4C, 使用atrId和idForPart=toUserId查询AccountJiFenChangeState记录，
		如果不存在，说明第4步尚未开始或未能成功开始，简单做4--6步
		如果存在，看AccountJiFenChangeState记录内容，
			如果state=didOut，说明第4步已经成功完成，由第5步来检查处理，见5C.
			如果state=didRollback,说明应该回滚了，转入 B 回滚逻辑 的处理流程
			如果state=其他，按业务逻辑分析不应该出现，给开发人员报错提醒是bug。
	5C, 使用atrId和idForPart=toUserId查询AccountYuEChangeState记录，
		如果不存在，说明第5步尚未开始或未能成功开始，简单做5--6步
		如果存在，看AccountYuEChangeState记录内容，
			如果state=didOut，说明第5步已经成功完成，由第6步来检查处理，见6C.
			如果state=didRollback,说明应该回滚了，转入 B 回滚逻辑 的处理流程
			如果state=其他，按业务逻辑分析不应该出现，给开发人员报错提醒是bug。
	6C,同第6步
	7C,释放分布式锁

***

## 小结
这种状态流转的分布式事务的实现的好处，我认为最大的好处就是直观。
所有的数据处理，都可以合在一个大的函数里面，可以看到整个处理过程的全貌。
这里也给出本人对前面的2个例子的具体代码实现，在 https://github.com/zlywq/tmycat1 。


觉得有用请打个赏吧
![觉得有用请打个赏吧](http://jianpuapk.oss-cn-hangzhou.aliyuncs.com/my/weixinPayQRcodeZlywq.png)








***


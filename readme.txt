




数据库建表sql在dbSchema.txt里面。
在 application.properties 里面设置数据连接。这个连接虽然可以指向单个mysql server，但正常是指向一个mycat server。
关于mycat server的配置可以参考 src\main\resources\mycat 里面的文件。
    需要说明的是，里面的mycat配置只配置了2个分片。使用5个分片的那个使用积分和余额付款的业务在这里只是示意。当然，如果读者不嫌麻烦可以在mycat中配置5个分片，这个代码同样（应该）可以正常运行。

关于 简单转账业务 的代码在 AppSimpleDistributedTran 。
关于使用5个分片的 使用积分和余额付款，积分和余额在不同的库中 的代码在 AppP5DistributedTran 。
有相关的单元测试。这里只是示意了主要路径。






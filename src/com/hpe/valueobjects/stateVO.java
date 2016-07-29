package com.hpe.valueobjects;
/*
 * Created by: pramod-reddy.sareddy@hpe.com
 * Class Name: stateVO
 * Description: To store the values of channel and queue to retrieve the messages from SCMB and MSMB(State Change Message Bus, Metrics Streaming Message Bus).
 * Date Created: 12-FEB-2016
 */


import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;

public class stateVO {
	public String queueName;
	public ConnectionFactory factory;
	public Channel channel;
	public Connection connection;
	public String consumerTag;
	public String getQueueName() {
		return queueName;
	}
	public void setQueueName(String queueName) {
		this.queueName = queueName;
	}
	
	public ConnectionFactory getFactory() {
		return factory;
	}
	public void setFactory(ConnectionFactory factory) {
		this.factory = factory;
	}
	public Channel getChannel() {
		return channel;
	}
	public void setChannel(Channel channel) {
		this.channel = channel;
	}
	public Connection getConnection() {
		return connection;
	}
	public void setConnection(Connection connection) {
		this.connection = connection;
	}
	public String getConsumerTag() {
		return consumerTag;
	}
	public void setConsumerTag(String consumerTag) {
		this.consumerTag = consumerTag;
	}
	
}

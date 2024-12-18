package com.example;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;

import io.lettuce.core.resource.ClientResources;
import io.lettuce.core.resource.DefaultClientResources;

import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.autoconfigure.data.redis.ClientResourcesBuilderCustomizer;
import org.springframework.boot.autoconfigure.data.redis.LettuceClientConfigurationBuilderCustomizer;
import org.springframework.boot.autoconfigure.data.redis.LettuceClientOptionsBuilderCustomizer;
import org.springframework.boot.autoconfigure.data.redis.RedisAutoConfiguration;
import org.springframework.boot.autoconfigure.data.redis.RedisConnectionDetails;
import org.springframework.boot.autoconfigure.data.redis.RedisProperties;
import org.springframework.boot.ssl.SslBundles;
import org.springframework.data.redis.connection.RedisClusterConfiguration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.RedisSentinelConfiguration;
import org.springframework.data.redis.connection.RedisStandaloneConfiguration;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.util.ClassUtils;

/**
 * @author Yanming Zhou
 */
public class RedisConfigurationSupport {

	private static final RedisAutoConfiguration redisAutoConfiguration = new RedisAutoConfiguration();

	private final Object lettuceConnectionConfiguration;

	RedisConfigurationSupport(RedisProperties properties,
			ObjectProvider<RedisStandaloneConfiguration> standaloneConfigurationProvider,
			ObjectProvider<RedisSentinelConfiguration> sentinelConfigurationProvider,
			ObjectProvider<RedisClusterConfiguration> clusterConfigurationProvider,
			RedisConnectionDetails connectionDetails, ObjectProvider<SslBundles> sslBundles) {
		try {
			Class<?> clazz = RedisAutoConfiguration.class;
			Class<?> configurationClass = ClassUtils.forName(clazz.getPackageName() + ".LettuceConnectionConfiguration",
					clazz.getClassLoader());
			Constructor<?> ctor = configurationClass.getDeclaredConstructor(
					RedisConfigurationSupport.class.getDeclaredConstructors()[0].getParameterTypes());
			ctor.setAccessible(true);
			this.lettuceConnectionConfiguration = ctor.newInstance(properties, standaloneConfigurationProvider,
					sentinelConfigurationProvider, clusterConfigurationProvider, connectionDetails, sslBundles);
		}
		catch (Exception ex) {
			throw new RuntimeException(ex.getMessage(), ex);
		}
	}

	protected DefaultClientResources lettuceClientResources(
			ObjectProvider<ClientResourcesBuilderCustomizer> customizers) {
		try {
			Method m = this.lettuceConnectionConfiguration.getClass()
				.getDeclaredMethod(getCurrentMethodName(), ObjectProvider.class);
			m.setAccessible(true);
			return (DefaultClientResources) m.invoke(this.lettuceConnectionConfiguration, customizers);
		}
		catch (Exception ex) {
			throw new RuntimeException(ex.getMessage(), ex);
		}
	}

	protected LettuceConnectionFactory redisConnectionFactory(
			ObjectProvider<LettuceClientConfigurationBuilderCustomizer> clientConfigurationBuilderCustomizers,
			ObjectProvider<LettuceClientOptionsBuilderCustomizer> clientOptionsBuilderCustomizers,
			ClientResources clientResources) {
		try {
			Method m = this.lettuceConnectionConfiguration.getClass()
				.getDeclaredMethod(getCurrentMethodName(), ObjectProvider.class, ObjectProvider.class,
						ClientResources.class);
			m.setAccessible(true);
			return (LettuceConnectionFactory) m.invoke(this.lettuceConnectionConfiguration,
					clientConfigurationBuilderCustomizers, clientOptionsBuilderCustomizers, clientResources);
		}
		catch (Exception ex) {
			throw new RuntimeException(ex.getMessage(), ex);
		}
	}

	protected RedisTemplate<Object, Object> redisTemplate(RedisConnectionFactory redisConnectionFactory) {
		return redisAutoConfiguration.redisTemplate(redisConnectionFactory);
	}

	protected StringRedisTemplate stringRedisTemplate(RedisConnectionFactory redisConnectionFactory) {
		return redisAutoConfiguration.stringRedisTemplate(redisConnectionFactory);
	}

	static RedisConnectionDetails createRedisConnectionDetails(RedisProperties properties) {
		try {
			Constructor<?> ctor = ClassUtils
				.forName(RedisProperties.class.getPackageName() + ".PropertiesRedisConnectionDetails",
						RedisProperties.class.getClassLoader())
				.getDeclaredConstructor(RedisProperties.class);
			ctor.setAccessible(true);
			return (RedisConnectionDetails) ctor.newInstance(properties);
		}
		catch (Exception ex) {
			throw new RuntimeException(ex.getMessage(), ex);
		}
	}

	private static String getCurrentMethodName() {
		return Thread.currentThread().getStackTrace()[2].getMethodName();
	}

}

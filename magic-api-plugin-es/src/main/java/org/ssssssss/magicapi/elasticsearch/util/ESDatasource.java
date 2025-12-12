package org.ssssssss.magicapi.elasticsearch.util;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import org.apache.http.HttpHost;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.apache.http.impl.nio.client.HttpAsyncClientBuilder;
import org.elasticsearch.client.Node;
import org.elasticsearch.client.NodeSelector;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestClient;
import org.elasticsearch.client.RestClientBuilder;
import org.elasticsearch.client.RestHighLevelClient;
import org.springframework.util.Assert;
import org.springframework.util.StringUtils;
import org.ssssssss.magicapi.elasticsearch.model.ESInfo;

public class ESDatasource {

    private String id = "";
    private RestHighLevelClient restHighLevelClient;
    private ESInfo esInfo;  // 存储配置信息，用于重连

    public String getId() {
        return id;
    }

    public void close() throws IOException {
        if (restHighLevelClient != null) {
            restHighLevelClient.close();
        }
    }

    public RestHighLevelClient getRestHighLevelClient() {
        return restHighLevelClient;
    }

    public ESDatasource(ESInfo info) {
        this.esInfo = info;  // 保存配置信息
        initRestClient(info);
    }
    
    /**
     * 验证 ES 客户端连接是否有效
     * @return true 表示连接成功，false 表示连接失败
     */
    public boolean ping() {
        if (restHighLevelClient == null) {
            return false;
        }
        try {
            return restHighLevelClient.ping(RequestOptions.DEFAULT);
        } catch (IOException e) {
            return false;
        }
    }

    /**
     * 初始化或重新初始化 Elasticsearch 客户端
     */
    private synchronized void initRestClient(ESInfo info) {
        try {
            if (restHighLevelClient != null) {
                restHighLevelClient.close();  // 关闭旧连接
            }

            Map<String, Object> properties = new HashMap<>(info.getProperties());
            String address = properties.get("address").toString();
            String userName = properties.get("username").toString();
            String password = properties.get("password").toString();
            int connectTimeout = Integer.parseInt(properties.get("connectTimeout").toString());
            int socketTimeout = Integer.parseInt(properties.get("socketTimeout").toString());
            int connectionRequestTimeout = Integer.parseInt(properties.get("connectionRequestTimeout").toString());
            int maxConnTotal = Integer.parseInt(properties.get("maxConnTotal").toString());
            int maxConnPerRoute = Integer.parseInt(properties.get("maxConnPerRoute").toString());

            restHighLevelClient = new RestHighLevelClient(
                restClientBuilder(address, userName, password, connectTimeout, socketTimeout,
                    connectionRequestTimeout, maxConnTotal, maxConnPerRoute)
            );
        } catch (IOException e) {
            throw new RuntimeException("Failed to initialize Elasticsearch client", e);
        }
    }

    /**
     * 自动重连机制（外部可调用）
     */
    public void reconnect() {
        initRestClient(esInfo);
    }

    /**
     * 获取 Elasticsearch 的 HTTP 主机地址
     */
    private HttpHost[] getElasticSearchHttpHosts(String address) {
        String[] hosts = address.split(",");
        HttpHost[] httpHosts = new HttpHost[hosts.length];
        for (int i = 0; i < httpHosts.length; i++) {
            String host = hosts[i];
            host = host.replaceAll("http://", "").replaceAll("https://", "");
            Assert.isTrue(host.contains(":"),
                String.format("Host %s format error. Expected format: 127.0.0.1:9200", host));
            httpHosts[i] = new HttpHost(host.split(":")[0], Integer.parseInt(host.split(":")[1]), "http");
        }
        return httpHosts;
    }

    /**
     * 配置 HTTP 异步客户端（含认证和连接池）
     */
    private HttpAsyncClientBuilder getHttpAsyncClientBuilder(
        HttpAsyncClientBuilder httpClientBuilder,
        String userName,
        String password,
        int maxConnTotal,
        int maxConnPerRoute
    ) {
        if (!StringUtils.isEmpty(userName) && !StringUtils.isEmpty(password)) {
            CredentialsProvider credentialsProvider = new BasicCredentialsProvider();
            credentialsProvider.setCredentials(
                AuthScope.ANY,
                new UsernamePasswordCredentials(userName, password)
            );
            httpClientBuilder.setDefaultCredentialsProvider(credentialsProvider);
        }
        httpClientBuilder.setMaxConnTotal(maxConnTotal);
        httpClientBuilder.setMaxConnPerRoute(maxConnPerRoute);
        return httpClientBuilder;
    }

    /**
     * 构建 RestClientBuilder（含重试和健康检查）
     */
    private RestClientBuilder restClientBuilder(
        String address,
        String userName,
        String password,
        int connectTimeout,
        int socketTimeout,
        int connectionRequestTimeout,
        int maxConnTotal,
        int maxConnPerRoute
    ) {
        HttpHost[] httpHosts = getElasticSearchHttpHosts(address);

        return RestClient.builder(httpHosts)
            .setRequestConfigCallback(requestConfigBuilder -> {
                requestConfigBuilder.setConnectTimeout(connectTimeout);
                requestConfigBuilder.setSocketTimeout(socketTimeout);
                requestConfigBuilder.setConnectionRequestTimeout(connectionRequestTimeout);
                return requestConfigBuilder;
            })
            .setFailureListener(new RestClient.FailureListener() {
                @Override
                public void onFailure(Node node) {
                    System.err.println("[Elasticsearch] Node failed: " + node.getName());
                    // 触发自动重连（延迟 5 秒）
                    new Thread(() -> {
                        try {
                            TimeUnit.SECONDS.sleep(5);
                            reconnect();
                        } catch (InterruptedException e) {
                            Thread.currentThread().interrupt();
                        }
                    }).start();
                }
            })
            .setHttpClientConfigCallback(httpClientBuilder -> {
                httpClientBuilder.disableAuthCaching();
                return getHttpAsyncClientBuilder(httpClientBuilder, userName, password, maxConnTotal, maxConnPerRoute);
            })
            // 启用节点健康检查（默认 1 分钟）
            .setNodeSelector(NodeSelector.SKIP_DEDICATED_MASTERS);
    }
}
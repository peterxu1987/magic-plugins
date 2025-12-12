package org.ssssssss.magicapi.elasticsearch;

import java.io.IOException;
import java.util.List;
import java.util.Map;

import org.apache.http.util.EntityUtils;
import org.elasticsearch.ElasticsearchException;
import org.elasticsearch.action.admin.indices.delete.DeleteIndexRequest;
import org.elasticsearch.action.admin.indices.mapping.get.GetMappingsRequest;
import org.elasticsearch.action.admin.indices.mapping.get.GetMappingsResponse;
import org.elasticsearch.action.admin.indices.settings.get.GetSettingsRequest;
import org.elasticsearch.action.admin.indices.settings.get.GetSettingsResponse;
import org.elasticsearch.action.bulk.BulkRequest;
import org.elasticsearch.action.bulk.BulkResponse;
import org.elasticsearch.action.get.GetRequest;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.action.index.IndexResponse;
import org.elasticsearch.action.support.master.AcknowledgedResponse;
import org.elasticsearch.action.update.UpdateRequest;
import org.elasticsearch.action.update.UpdateResponse;
import org.elasticsearch.client.Request;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.Response;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.client.indices.CreateIndexRequest;
import org.elasticsearch.client.indices.CreateIndexResponse;
import org.elasticsearch.client.indices.GetIndexRequest;
import org.elasticsearch.common.xcontent.XContentBuilder;
import org.elasticsearch.common.xcontent.XContentFactory;
import org.elasticsearch.common.xcontent.XContentType;
import org.elasticsearch.rest.RestStatus;
import org.elasticsearch.search.fetch.subphase.FetchSourceContext;
import org.springframework.util.CollectionUtils;
import org.ssssssss.magicapi.core.annotation.MagicModule;
import org.ssssssss.magicapi.elasticsearch.model.MagicDynamicESClient;
import org.ssssssss.script.annotation.Comment;
import org.ssssssss.script.functions.DynamicAttribute;

import com.alibaba.fastjson.JSON;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@MagicModule("es")
public class ESModule implements DynamicAttribute<ESModule, ESModule> {
	private RestHighLevelClient restHighLevelClient;
	private MagicDynamicESClient magicDynamicESClient;
	
	public ESModule(RestHighLevelClient restHighLevelClient) {
		this.restHighLevelClient = restHighLevelClient;
	}
	
	public ESModule(MagicDynamicESClient magicDynamicESClient) {
		this.magicDynamicESClient = magicDynamicESClient;
	}

	@Override
	public ESModule getDynamicAttribute(String key) {
		return magicDynamicESClient.getESModule(key);
	}

	@Comment( "/**  \n\n"+
			 "* 判断索引是否存在  \n\n"+
			 "*  \n\n"+
			 "* @param index 索引  \n\n"+
			 "* @return 返回 true，表示存在  \n\n"+
			 "*/  \n\n")
    public boolean existsIndex(@Comment(name = "index", value = "索引名称") String index) {
        try {
            GetIndexRequest request = new GetIndexRequest(index);
            request.local(false);
            request.humanReadable(true);
            request.includeDefaults(false);

            return restHighLevelClient.indices().exists(request, RequestOptions.DEFAULT);
        } catch (IOException e) {
            log.error("[ elasticsearch ] >> get index exists exception ,index:{} ", index, e);
            throw new ElasticSearchRunException("[ elasticsearch ] >> get index exists exception {}", e);
        }
    }
	

	@Comment("/**  \n\n"+
    		"     * 创建 ES 索引  \n\n"+
    		"     *  \n\n"+
    		"     * @param index      索引  \n\n"+
    		"     * @param shards     分片数量  \n\n"+
    		"     * @param replicas   分片的副本数量  \n\n"+
    		"     * @param properties 文档属性  \n\n"+
    		"     * 			{  \n\n"+
    		"     * 				fieldName:{  \n\n"+
    		"     * 					\"type\": \"text/keyword/integer/long/double/date/true/false/binary\"  #这里是类型  \n\n"+
    		"     * 					\"ignore_above\": 256  #这里限制大小  \n\n"+
    		"     * 				},  \n\n"+
    		"     * 				......  \n\n"+
    		"     * 			}  \n\n"+
    		"     * @return 返回 true，表示创建成功  \n\n"+
    		"     */  \n\n")
    public boolean createIndex(
    		@Comment(name = "index", value = "索引名称") String index,
    		@Comment(name = "shards", value = "分片数量") int shards,
    		@Comment(name = "replicas", value = "分片的副本数量") int replicas,
    		@Comment(name = "properties", value = "文档属性") Map<String, Object> properties
    ) {
        try {
            XContentBuilder builder = XContentFactory.jsonBuilder();
            // 注：ES 7.x 后的版本中，已经弃用 type
            builder.startObject()
                    .startObject("mappings")
                    .field("properties", properties)
                    .endObject()
                    .startObject("settings")
                    //分片数
                    .field("number_of_shards", shards)
                    //副本数
                    .field("number_of_replicas", replicas)
                    .endObject()
                    .endObject();
            CreateIndexRequest request = new CreateIndexRequest(index).source(builder);
            CreateIndexResponse response = restHighLevelClient.indices().create(request, RequestOptions.DEFAULT);
            return response.isAcknowledged();
        } catch (IOException e) {
            log.error("[ elasticsearch ] >> createIndex exception ,index:{},properties:{}", index, properties, e);
            throw new ElasticSearchRunException("[ elasticsearch ] >> createIndex exception ");
        }
    }
    
	@Comment("/**  \n\n"+
    		"     * 创建 ES 索引  \n\n"+
    		"     *  \n\n"+
    		"     * @param index      索引  \n\n"+
    		"     * @param shards     分片数量  \n\n"+
    		"     * @param replicas   分片的副本数量  \n\n"+
    		"     * @return 返回 true，表示创建成功  \n\n"+
    		"     */  \n\n")
    public boolean createIndex(
    		@Comment(name = "index", value = "索引名称") String index,
    		@Comment(name = "shards", value = "分片数量") int shards,
    		@Comment(name = "replicas", value = "分片的副本数量") int replicas
    ) {
    	try {
    		XContentBuilder builder = XContentFactory.jsonBuilder();
            // 注：ES 7.x 后的版本中，已经弃用 type
            builder.startObject()
                    .startObject("settings")
                    //分片数
                    .field("number_of_shards", shards)
                    //副本数
                    .field("number_of_replicas", replicas)
                    .endObject()
                    .endObject();
            CreateIndexRequest request = new CreateIndexRequest(index).source(builder);
            CreateIndexResponse response = restHighLevelClient.indices().create(request, RequestOptions.DEFAULT);
            return response.isAcknowledged();
    	} catch (IOException e) {
    		throw new ElasticSearchRunException("[ elasticsearch ] >> createIndex exception ");
    	}
    }
  
	@Comment("/**  \n\n"+
    		"     * 删除索引  \n\n"+
    		"     *  \n\n"+
    		"     * @param index      索引  \n\n"+
    		"     * @return 返回 true，表示创建成功  \n\n"+
    		"     */  \n\n")
    public boolean deleteIndex(@Comment(name = "index", value = "索引名称") String index) {
        try {
            DeleteIndexRequest request = new DeleteIndexRequest(index);
            AcknowledgedResponse response = restHighLevelClient.indices().delete(request, RequestOptions.DEFAULT);

            return response.isAcknowledged();
        } catch (ElasticsearchException e) {
            //索引不存在-无需删除
            if (e.status() == RestStatus.NOT_FOUND) {
                log.error("[ elasticsearch ] >>  deleteIndex >>  index:{}, Not found ", index, e);
                return false;
            }
            log.error("[ elasticsearch ] >> deleteIndex exception ,index:{}", index, e);
            throw new ElasticSearchRunException("elasticsearch deleteIndex exception ");
        } catch (IOException e) {
            //其它未知异常
            log.error("[ elasticsearch ] >> deleteIndex exception ,index:{}", index, e);
            throw new ElasticSearchRunException("[ elasticsearch ] >>  deleteIndex exception {}", e);
        }
    }

	@SuppressWarnings("unchecked")
	@Comment("/**  \n\n"+
    		"     * 获取索引setting配置  \n\n"+
    		"     *  \n\n"+
    		"     * @param index      索引  \n\n"+
    		"     * @return 返回索引配置内容  \n\n"+
    		"     */  \n\n")
    public Object getIndexSetting(@Comment(name = "index", value = "索引名称") String index) {
        try {
            GetSettingsRequest request = new GetSettingsRequest().indices(index);
            GetSettingsResponse getSettingsResponse = restHighLevelClient.indices().getSettings(request, RequestOptions.DEFAULT);
            
            Map<String,Map<String,Map<String,Map<String, Object>>>> map = JSON.parseObject(getSettingsResponse.toString(), Map.class);
            return map.get(index).get("settings").get("index");
            
        } catch (IOException e) {
            //其它未知异常
            log.error("[ elasticsearch ] >> getIndexSetting exception ,index:{}", index, e);
            throw new ElasticSearchRunException("[ elasticsearch ] >>  getIndexSetting exception {}", e);
        }
    }
	
	@Comment("/**  \n\n"+
			"     * 获取索引mapping配置  \n\n"+
			"     *  \n\n"+
			"     * @param index      索引  \n\n"+
			"     * @return 返回索引配置内容  \n\n"+
			"     */  \n\n")
	public Object getIndexMapping(@Comment(name = "index", value = "索引名称") String index) {
		try {
			
			GetMappingsRequest request = new GetMappingsRequest().indices(index);
			@SuppressWarnings("deprecation")
			GetMappingsResponse getMappingsResponse = restHighLevelClient.indices().getMapping(request, RequestOptions.DEFAULT);
			 @SuppressWarnings("unchecked")
			Map<String,Map<String,Map<String,Map<String, Object>>>> map = JSON.parseObject(getMappingsResponse.toString(), Map.class);
			
			return map.get(index).get("mappings").get("properties");
			
		} catch (IOException e) {
			//其它未知异常
			log.error("[ elasticsearch ] >> getIndexSetting exception ,index:{}", index, e);
			throw new ElasticSearchRunException("[ elasticsearch ] >>  getIndexSetting exception {}", e);
		}
	}

	@Comment("/**  \n\n"+
    		"     * 判断索引中是否存在某条数据-根据编号  \n\n"+
    		"     *  \n\n"+
    		"     * @param index      索引  \n\n"+
    		"     * @return true/false \n\n"+
    		"     */  \n\n")
    public boolean existsDocument(
    		@Comment(name = "index", value = "索引名称") String index, 
    		@Comment(name = "id", value = "数据编号") String id) {
        try {
            GetRequest request = new GetRequest(index, id);
            //禁用获取_source
            request.fetchSourceContext(new FetchSourceContext(false));
            //禁用获取存储的字段。
            request.storedFields("_none_");

            return restHighLevelClient.exists(request, RequestOptions.DEFAULT);
        } catch (IOException e) {
            log.error("[ elasticsearch ] >> get document exists exception ,index:{} ", index, e);
            throw new ElasticSearchRunException("[ elasticsearch ] >> get document  exists exception {}", e);
        }
    }
 
	@Comment("/**  \n\n"+
    		"     * 保存数据-随机生成数据ID  \n\n"+
    		"     *  \n\n"+
    		"     * @param index      索引  \n\n"+
    		"     * @param dataValue 数据内容 \n\n"+
    		"     * @return 数据结果 \n\n"+
    		"     *         {id:\"自动生产的id\",type:\"数据操作的类型（create、update...）\"} \n\n"+
    		"     */  \n\n")
    public Object save(@Comment(name = "index", value = "索引名称") String index, 
    		@Comment(name = "dataValue", value = "数据内容") Object dataValue) {
        try {
            IndexRequest request = new IndexRequest(index);
            request.source(JSON.toJSONString(dataValue), XContentType.JSON);
            IndexResponse indexResponse = restHighLevelClient.index(request, RequestOptions.DEFAULT);
            return JSON.parseObject("{\"id\":\""+indexResponse.getId()+"\",\"type\":\""+indexResponse.getResult()+"\"}", Map.class);
        } catch (IOException e) {
            log.error("[ elasticsearch ] >> save exception ,index = {},dataValue={} ,stack={}", index, dataValue, e);
            throw new ElasticSearchRunException("[ elasticsearch ] >> save exception {}", e);
        }
    }

	@Comment("/**  \n\n"+
    		"     * 保存文档-自定义数据ID  \n\n"+
    		"     *  \n\n"+
    		"     * @param index      索引  \n\n"+
    		"     * @param id         数据id  \n\n"+
    		"     * @param dataValue 数据内容 \n\n"+
    		"     * @return  数据结果 \n\n"+
    		"     *         {id:\"自动生产的id\",type:\"数据操作的类型（create、update）\"} \n\n"+
    		"     */  \n\n")
    public Object save(
    		@Comment(name = "index", value = "索引名称") String index, 
    		@Comment(name = "id", value = "索引名称") String id, 
    		@Comment(name = "dataValue", value = "数据内容") Object dataValue) {
        IndexResponse indexResponse = this.saveOrUpdate(index, id, dataValue);
        return JSON.parseObject("{\"id\":\""+indexResponse.getId()+"\",\"type\":\""+indexResponse.getResult()+"\"}", Map.class);
    }

    /**
     * 保存文档-自定义数据ID
     * <p>
     * 如果文档存在，则更新文档；如果文档不存在，则保存文档。
     *
     * @param index     索引
     * @param id        数据ID
     * @param dataValue 数据内容
     */
    private IndexResponse saveOrUpdate(String index, String id, Object dataValue) {
        try {
            IndexRequest request = new IndexRequest(index);
            request.id(id);
            request.source(JSON.toJSONString(dataValue), XContentType.JSON);
            return restHighLevelClient.index(request, RequestOptions.DEFAULT);
        } catch (IOException e) {
            log.error("[ elasticsearch ] >> save exception ,index = {},dataValue={} ,stack={}", index, dataValue, e);
            throw new ElasticSearchRunException("[ elasticsearch ] >> save exception {}", e);
        }
    }

    @Comment("/**  \n\n"+
    		" * 批量-更新或保存文档数据  \n\n"+
    		" * 	如果集合中有些文档已经存在，则更新文档；不存在，则保存文档。  \n\n"+
    		" *  \n\n"+
    		" * @param index        索引  \n\n"+
    		" * @param documentList 数据集合  \n\n"+
    		" * 			[  \n\n"+
    		" * 				{  \n\n"+
    		" * 					\"id\": ${id},  \n\n"+
    		" * 					\"data\": {  \n\n"+
    		" * 						\"id\":\"xxx\",  \n\n"+
    		" * 						\"name\":\"xxx\",  \n\n"+
    		" * 						......  \n\n"+
    		" * 					}  \n\n"+
    		" * 				}  \n\n"+
    		" * 			]  \n\n"+
    		" */  \n\n")
    public Boolean batchSaveOrUpdate(
    		@Comment(name = "index", value = "索引名称") String index, 
    		@Comment(name = "documentList", value = "数据集合") List<Map<String,Object>> documentList) {
        if (CollectionUtils.isEmpty(documentList)) {
        	throw new ElasticSearchRunException("[ elasticsearch ] >> batchSave exception {}", "documentList 值为空！");
        }
        try {
        	// 批量请求
            BulkRequest bulkRequest = new BulkRequest();
        	for (Map<String,Object> elasticSearchDocModel : documentList) {
        		bulkRequest.add(new IndexRequest(index)
                        .id(elasticSearchDocModel.get("id").toString())
                        .source(JSON.toJSONString(elasticSearchDocModel.get("data")), XContentType.JSON));
        		
			}
        	 BulkResponse bulkResponse = restHighLevelClient.bulk(bulkRequest, RequestOptions.DEFAULT);
        	 if(bulkResponse.hasFailures()) {
        		 return false;
        	 }
             return true;
        } catch (IOException e) {
        	e.printStackTrace();
            log.error("[ elasticsearch ] >> batchSave exception ,index = {},documentList={} ,stack={}", index, documentList, e);
            throw new ElasticSearchRunException("[ elasticsearch ] >> batchSave exception {}", e);
        }
    }

    @Comment("/**  \n\n"+
    		"     * 根据ID修改数据 \n\n"+
    		"     *  \n\n"+
    		"     * @param index      索引  \n\n"+
    		"     * @param id         数据id  \n\n"+
    		"     * @param dataValue 数据内容 \n\n"+
    		"     * @return 数据结果 \n\n"+
    		"     */  \n\n")
    public UpdateResponse updateById(
    		@Comment(name = "index", value = "索引名称") String index, 
    		@Comment(name = "id", value = "索引名称") String id, 
    		@Comment(name = "dataValue", value = "数据内容") Object dataValue) {
        try {
            UpdateRequest request = new UpdateRequest(index, id);
            request.doc(JSON.toJSONString(dataValue), XContentType.JSON);
            return restHighLevelClient.update(request, RequestOptions.DEFAULT);
        } catch (IOException e) {
            log.error("[ elasticsearch ] >> updateById exception ,index = {},dataValue={} ,stack={}", index, dataValue, e);
            throw new ElasticSearchRunException("[ elasticsearch ] >> updateById exception {}", e);
        }
    }
    
    @Comment("/**  \n\n"+
    		"     * 删除数据-根据sql \n\n"+
    		"     *  \n\n"+
    		"     * @param sql      标准sql 语句  \n\n"+
    		"     * @return   数据内容 \n\n"+
    		"     */  \n\n")
    public Object delete(
    		@Comment(name = "sql", value = "sql") String sql
    		
    		) throws IOException {
        String endpoint = "/_nlpcn/sql";
        String requestBody = sql;

        Request request = new Request("POST", endpoint);
        request.setJsonEntity(requestBody);

        Response response = restHighLevelClient.getLowLevelClient().performRequest(request);

        return JSON.parseObject(EntityUtils.toString(response.getEntity()), Map.class);
    }
    @Comment("/**  \n\n"+
    		"     * Post 查询数据-根据sql \n\n"+
    		"     *  \n\n"+
    		"     * @param sql      标准sql 语句  \n\n"+
    		"     * @return   数据内容 \n\n"+
    		"     */  \n\n")
    public Object query(
    		@Comment(name = "sql", value = "sql") String sql
    		
    		) throws IOException {
    	String endpoint = "/_nlpcn/sql";
    	String requestBody = sql;
    	
    	Request request = new Request("POST", endpoint);
    	request.setJsonEntity(requestBody);
    	
    	Response response = restHighLevelClient.getLowLevelClient().performRequest(request);
    	
    	return JSON.parseObject(EntityUtils.toString(response.getEntity()), Map.class);
    }
    @Comment("/**  \n\n"+
    		"     * Get 查询数据-根据sql \n\n"+
    		"     *  \n\n"+
    		"     * @param sql      标准sql 语句  \n\n"+
    		"     * @return   数据内容 \n\n"+
    		"     */  \n\n")
    public Object queryByGet(
    		@Comment(name = "sql", value = "sql") String sql
    		
    		) throws IOException {
    	String endpoint = "/_nlpcn/sql";
    	String requestBody = sql;
    	
    	Request request = new Request("GET", endpoint);
    	request.setJsonEntity(requestBody);
    	
    	Response response = restHighLevelClient.getLowLevelClient().performRequest(request);
    	
    	return JSON.parseObject(EntityUtils.toString(response.getEntity()), Map.class);
    }
    
    @Comment("/**  \n\n"+
    		"     * 查询数据-根据sql \n\n"+
    		"     *  \n\n"+
    		"     * @param sql      标准sql 语句  \n\n"+
    		"     * @param pageNo   当前页码  \n\n"+
    		"     * @param pageSize 每页显示条数  \n\n"+
    		"     * @return   数据内容 \n\n"+
    		"     */  \n\n")
    public Object pageQuery(
    		@Comment(name = "sql", value = "sql") String sql,
    		@Comment(name = "pageNo", value = "pageNo") int pageNo,
    		@Comment(name = "pageSize", value = "pageSize") int pageSize) throws IOException {
    	int from = (pageNo - 1) * pageSize;
   	    String _sql = String.format(sql+" LIMIT %d OFFSET %d", pageSize, from);
    	return query(_sql);
    }
}

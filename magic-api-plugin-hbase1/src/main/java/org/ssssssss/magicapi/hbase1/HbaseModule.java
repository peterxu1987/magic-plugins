package org.ssssssss.magicapi.hbase1;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import org.apache.hadoop.hbase.Cell;
import org.apache.hadoop.hbase.CellUtil;
import org.apache.hadoop.hbase.HColumnDescriptor;
import org.apache.hadoop.hbase.HTableDescriptor;
import org.apache.hadoop.hbase.TableName;
import org.apache.hadoop.hbase.client.Admin;
import org.apache.hadoop.hbase.client.Connection;
import org.apache.hadoop.hbase.client.Delete;
import org.apache.hadoop.hbase.client.Get;
import org.apache.hadoop.hbase.client.Put;
import org.apache.hadoop.hbase.client.Result;
import org.apache.hadoop.hbase.client.ResultScanner;
import org.apache.hadoop.hbase.client.Scan;
import org.apache.hadoop.hbase.client.Table;
import org.apache.hadoop.hbase.filter.CompareFilter.CompareOp;
import org.apache.hadoop.hbase.filter.Filter;
import org.apache.hadoop.hbase.filter.FilterList;
import org.apache.hadoop.hbase.filter.RegexStringComparator;
import org.apache.hadoop.hbase.filter.RowFilter;
import org.apache.hadoop.hbase.io.compress.Compression.Algorithm;
import org.apache.hadoop.hbase.regionserver.BloomType;
import org.apache.hadoop.hbase.util.Bytes;
import org.ssssssss.magicapi.core.annotation.MagicModule;
import org.ssssssss.magicapi.hbase1.model.MagicDynamicHbaseClient;
import org.ssssssss.script.annotation.Comment;
import org.ssssssss.script.functions.DynamicAttribute;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;

@MagicModule("hbase1")
public class HbaseModule implements DynamicAttribute<HbaseModule, HbaseModule> {
	
	private volatile Connection connection;
	private MagicDynamicHbaseClient magicDynamicHbaseClient;

	// 私有构造方法（单例模式）
	public HbaseModule(Connection connection) {
		this.connection = connection;
	}
	
	public HbaseModule(MagicDynamicHbaseClient magicDynamicHbaseClient) {
		this.magicDynamicHbaseClient = magicDynamicHbaseClient;
	}

	@Override
	public HbaseModule getDynamicAttribute(String key) {
		return magicDynamicHbaseClient.getModule(key);
	}
 
	@Comment("/**\n\n"
			+ "	*创建表  \n\n"
			+ "	* @param tableName  \n\n"
			+ "	* @param attrs 数组 格式{CF:xx,VERSIONS:xx,...}  \n\n"
			+ "	*		  \n\n"
			+ "	*		CF  \n\n"
			+ "	*			1、列族名称  \n\n"
			+ "	*		VERSIONS    ->  cell数据版本  \n\n"
			+ "	*			1、0.96的版本之前，默认每个列族是3个version。  \n\n"
			+ "	*			2、0.96之后，每个列族是1个version。  \n\n"
			+ "	*			3、在大合并时，会遗弃过期的版本。  \n\n"
			+ "	*		COMPRESSION    -> 压缩类型  \n\n"
			+ "	*			1、HFile可以被压缩并存放在HDFS上。这有助于节省硬盘IO，但是读写数据时压缩和解压缩会抬高CPU利用率。  \n\n"
			+ "	*			2、压缩是表定义的一部分，可以在建表或修改表结构时设定。建议打开表的压缩，除非你确定不会从压缩中受益。  \n\n"
			+ "	*			3、只有在数据不能被压缩或者因为某种原因服务器的CPU利用率有限制要求的情况下，有可能会关闭压缩特性。  \n\n"
			+ "	*			4、HBase可以使用多种压缩编码，包括LZO、SNAPPY和GZIP  \n\n"
			+ "	*		BLOCKSIZE    -> 数据块大小  \n\n"
			+ "	*			1、随机查询：数据块越小，索引越大，查找性能更好。  \n\n"
			+ "	*			2、顺序查询：更好的顺序扫描，需要更大的数据块。  \n\n"
			+ "	*			3、所以在使用的时候根据业务需求来判断是随机查询需求多还是顺序查询需求多，根据具体的场景而定。  \n\n"
			+ "	*		BLOCKCACHE    -> 数据块缓存  \n\n"
			+ "	*			1、如果一张表或表里的某个列族只被顺序化扫描访问或者很少被访问，这个时候就算Get或Scan花费时间是否有点儿长，  \n\n"
			+ "	*		  你也不会很在意。在这种情况下，你可以选择关闭那些列族的缓存。  \n\n"
			+ "	*			2、如果你只是执行很多顺序化扫描，你会多次倒腾缓存，并且可能会滥用缓存把应该放进缓存获得性能提升的数据给排挤出去。  \n\n"
			+ "	*		  如果关闭缓存，不仅可以避免上述情况发生，而且还可以让出更多缓存给其他表和同一个表的其他列族使用。  \n\n"
			+ "	*		BLOOMFILTER    -> 布隆过滤器  \n\n"
			+ "	*			1、HBase中存储额外的索引层次会占用额外的空间。布隆过滤器随着它们的索引对象的数据增长而增长，所以行级布隆过滤器比  \n\n"
			+ "	*		  列标识符级布隆过滤器占用空间要少。当空间不是问题的时候，它们可以帮助你榨干系统的性能潜力。  \n\n"
			+ "	*			2、BLOOMFILTER参数的默认值是ROW，表示是行级布隆过滤器。  \n\n"
			+ "	*			3、使用行级布隆过滤器需要设置为ROW，使用列标识符级布隆过滤器需要设置为ROWCOL。  \n\n"
			+ "	*			4、行级布隆过滤器在数据块里检查特定行键是否不存在，列标识符级布隆过滤器检查行和列标识符联合体是否不存在。  \n\n"
			+ "	*			5、ROWCOL布隆过滤器的开销要高于ROW布隆过滤器。  \n\n"
			+ "	*		TTL    -> 生存时间  \n\n"
			+ "	*			1、应用系统经常需要从数据库里删除老数据，配置此项，可使数据增加生命周期，超过该配置时间的数据，将会在大合并时  \n\n"
			+ "	*		  “被删除”。（单位：秒）  \n\n"
			+ "	* @throws Exception  \n\n"
			+ "	*  \n\n"
			+ "	*	hbase.createTable(\"student\", {'CF':'info'}::stringify,...);  \n\n"
			+ "	*	hbase.createTable(\"student\", {'CF':'info'}::stringify , {'CF':'level','VERSIONS':'3'}::stringify,...)\n\n"
			+ "	*/")
	public void createTable(
			@Comment(name = "tableName", value = "表名称") String tableName ,
			@Comment(name = "attrs", value = "String... 类型，具体使用看注释") String... attrs) throws Exception {

        Admin admin = connection.getAdmin();
        TableName tName = TableName.valueOf(tableName);

        // 1.验证表是否存在
        if (admin.tableExists(tName)) {
            System.out.println("表 " + tableName + " 已存在，准备删除重建...");
        } else {
            // 2. 创建表描述符
            HTableDescriptor tableDescriptor = new HTableDescriptor(TableName.valueOf(tableName));
            for (String attr : attrs) {
                JSONObject attrObj = JSON.parseObject(attr);
                Object cf = attrObj.get("CF");
                Object versions = attrObj.get("VERSIONS");
                Object compression = attrObj.get("COMPRESSION");
                Object blocksize = attrObj.get("BLOCKSIZE");
                Object blockcache = attrObj.get("BLOCKCACHE");
                Object bloomfilter = attrObj.get("BLOOMFILTER");
                Object ttl = attrObj.get("TTL");

                HColumnDescriptor cfBuilder = null;
                if (cf != null) {
                    cfBuilder = new HColumnDescriptor(cf.toString());
                }
                if (versions != null) {
                    cfBuilder.setMaxVersions(Integer.parseInt(versions.toString()));
                }
                if (compression != null) {
                    if (compression.toString().equals("SNAPPY")) {
                        cfBuilder.setCompressionType(Algorithm.SNAPPY);
                    } else if (compression.toString().equals("LZO")) {
                        cfBuilder.setCompressionType(Algorithm.LZO);
                    } else if (compression.toString().equals("GZIP")) {
                        cfBuilder.setCompressionType(Algorithm.GZ);
                    }
                }
                if (blocksize != null) {
                    cfBuilder.setBlocksize(Integer.parseInt(blocksize.toString()));
                }
                if (blockcache != null) {
                    if (blockcache.toString().equals("true")) {
                        cfBuilder.setBlockCacheEnabled(true);
                    } else {
                        cfBuilder.setBlockCacheEnabled(false);
                    }
                }
                if (bloomfilter != null) {
                    if (bloomfilter.toString().equals("ROWCOL")) {
                        cfBuilder.setBloomFilterType(BloomType.ROWCOL);
                    } else if (bloomfilter.toString().equals("ROW")) {
                        cfBuilder.setBloomFilterType(BloomType.ROW);
                    }
                }
                if (ttl != null) {
                    cfBuilder.setTimeToLive(Integer.parseInt(ttl.toString()));
                }
                // 4. 将列族加入表描述符
                tableDescriptor.addFamily(cfBuilder);
            }

            // 5. 创建表
            admin.createTable(tableDescriptor);
            System.out.println("表 " + tableName + " 创建成功！");
            admin.close();
        }

    }
	
	
	@Comment("/**\n\n"
			+ "	* 修改列族属性  \n\n"
			+ "	* @param tableName  \n\n"
			+ "	* @param attrs 数组 格式{CF:xx,VERSIONS:xx,...}  \n\n"
			+ "	*		  \n\n"
			+ "	*		CF  \n\n"
			+ "	*			1、列族名称  \n\n"
			+ "	*		VERSIONS    ->  cell数据版本  \n\n"
			+ "	*			1、0.96的版本之前，默认每个列族是3个version。  \n\n"
			+ "	*			2、0.96之后，每个列族是1个version。  \n\n"
			+ "	*		3、在大合并时，会遗弃过期的版本。  \n\n"
			+ "	*		COMPRESSION    -> 压缩类型  \n\n"
			+ "	*			1、HFile可以被压缩并存放在HDFS上。这有助于节省硬盘IO，但是读写数据时压缩和解压缩会抬高CPU利用率。  \n\n"
			+ "	*			2、压缩是表定义的一部分，可以在建表或修改表结构时设定。建议打开表的压缩，除非你确定不会从压缩中受益。  \n\n"
			+ "	*			3、只有在数据不能被压缩或者因为某种原因服务器的CPU利用率有限制要求的情况下，有可能会关闭压缩特性。  \n\n"
			+ "	*			4、HBase可以使用多种压缩编码，包括LZO、SNAPPY和GZIP  \n\n"
			+ "	*		BLOCKSIZE    -> 数据块大小  \n\n"
			+ "	*			1、随机查询：数据块越小，索引越大，查找性能更好。  \n\n"
			+ "	*			2、顺序查询：更好的顺序扫描，需要更大的数据块。  \n\n"
			+ "	*			3、所以在使用的时候根据业务需求来判断是随机查询需求多还是顺序查询需求多，根据具体的场景而定。  \n\n"
			+ "	*		BLOCKCACHE    -> 数据块缓存  \n\n"
			+ "	*			1、如果一张表或表里的某个列族只被顺序化扫描访问或者很少被访问，这个时候就算Get或Scan花费时间是否有点儿长，  \n\n"
			+ "	*		  你也不会很在意。在这种情况下，你可以选择关闭那些列族的缓存。  \n\n"
			+ "	*			2、如果你只是执行很多顺序化扫描，你会多次倒腾缓存，并且可能会滥用缓存把应该放进缓存获得性能提升的数据给排挤出去。  \n\n"
			+ "	*		  如果关闭缓存，不仅可以避免上述情况发生，而且还可以让出更多缓存给其他表和同一个表的其他列族使用。  \n\n"
			+ "	*		BLOOMFILTER    -> 布隆过滤器  \n\n"
			+ "	*			1、HBase中存储额外的索引层次会占用额外的空间。布隆过滤器随着它们的索引对象的数据增长而增长，所以行级布隆过滤器比  \n\n"
			+ "	*		  列标识符级布隆过滤器占用空间要少。当空间不是问题的时候，它们可以帮助你榨干系统的性能潜力。  \n\n"
			+ "	*			2、BLOOMFILTER参数的默认值是ROW，表示是行级布隆过滤器。  \n\n"
			+ "	*			3、使用行级布隆过滤器需要设置为ROW，使用列标识符级布隆过滤器需要设置为ROWCOL。  \n\n"
			+ "	*			4、行级布隆过滤器在数据块里检查特定行键是否不存在，列标识符级布隆过滤器检查行和列标识符联合体是否不存在。  \n\n"
			+ "	*			5、ROWCOL布隆过滤器的开销要高于ROW布隆过滤器。  \n\n"
			+ "	*		TTL    -> 生存时间  \n\n"
			+ "	*			1、应用系统经常需要从数据库里删除老数据，配置此项，可使数据增加生命周期，超过该配置时间的数据，将会在大合并时  \n\n"
			+ "	*		  “被删除”。（单位：秒）  \n\n"
			+ "	* @throws Exception  \n\n"
			+ "	*  \n\n"
			+ "	*   hbase.modifyColumnFamilyAttr(\"student\", {'CF':'info','COMPRESSION':'SNAPPY'}::stringify,...);\n\n"
			+ "	*/")
	public void modifyColumnFamilyAttr(
			@Comment(name = "tableName", value = "表名称") String tableName ,
			@Comment(name = "attrs", value = "String... 类型，具体使用看注释") String... attrs
			) throws Exception {
        Admin admin = connection.getAdmin();
        for (String attr : attrs) {
            JSONObject attrObj = JSON.parseObject(attr);
            Object cf = attrObj.get("CF");
            Object versions = attrObj.get("VERSIONS");
            Object compression = attrObj.get("COMPRESSION");
            Object blocksize = attrObj.get("BLOCKSIZE");
            Object blockcache = attrObj.get("BLOCKCACHE");
            Object bloomfilter = attrObj.get("BLOOMFILTER");
            Object ttl = attrObj.get("TTL");
            HColumnDescriptor cfBuilder = null;
            if (cf != null) {
                cfBuilder = new HColumnDescriptor(cf.toString());
            }
            if (versions != null) {
                cfBuilder.setMaxVersions(Integer.parseInt(versions.toString()));
            }
            if (compression != null) {
                if (compression.toString().equals("SNAPPY")) {
                    cfBuilder.setCompressionType(Algorithm.SNAPPY);
                } else if (compression.toString().equals("LZO")) {
                    cfBuilder.setCompressionType(Algorithm.LZO);
                } else if (compression.toString().equals("GZIP")) {
                    cfBuilder.setCompressionType(Algorithm.GZ);
                }
            }
            if (blocksize != null) {
                cfBuilder.setBlocksize(Integer.parseInt(blocksize.toString()));
            }
            if (blockcache != null) {
                if (blockcache.toString().equals("true")) {
                    cfBuilder.setBlockCacheEnabled(true);
                } else {
                    cfBuilder.setBlockCacheEnabled(false);
                }
            }
            if (bloomfilter != null) {
                if (bloomfilter.toString().equals("ROWCOL")) {
                    cfBuilder.setBloomFilterType(BloomType.ROWCOL);
                } else if (bloomfilter.toString().equals("ROW")) {
                    cfBuilder.setBloomFilterType(BloomType.ROW);
                }
            }
            if (ttl != null) {
                cfBuilder.setTimeToLive(Integer.parseInt(ttl.toString()));
            }
            admin.modifyColumn(TableName.valueOf(tableName), cfBuilder);
        }
        admin.close();
    }
	
	@Comment("/*\n\n"
			+ "	*追加列族  \n\n"
			+ "	* @param tableName  \n\n"
			+ "	* @param attrs 数组 格式{CF:xx,VERSIONS:xx,...}  \n\n"
			+ "	*		  \n\n"
			+ "	*		CF  \n\n"
			+ "	*			1、列族名称  \n\n"
			+ "	*		VERSIONS    ->  cell数据版本  \n\n"
			+ "	*			1、0.96的版本之前，默认每个列族是3个version。  \n\n"
			+ "	*			2、0.96之后，每个列族是1个version。  \n\n"
			+ "	*			3、在大合并时，会遗弃过期的版本。  \n\n"
			+ "	*		COMPRESSION    -> 压缩类型  \n\n"
			+ "	*			1、HFile可以被压缩并存放在HDFS上。这有助于节省硬盘IO，但是读写数据时压缩和解压缩会抬高CPU利用率。  \n\n"
			+ "	*			2、压缩是表定义的一部分，可以在建表或修改表结构时设定。建议打开表的压缩，除非你确定不会从压缩中受益。  \n\n"
			+ "	*			3、只有在数据不能被压缩或者因为某种原因服务器的CPU利用率有限制要求的情况下，有可能会关闭压缩特性。  \n\n"
			+ "	*			4、HBase可以使用多种压缩编码，包括LZO、SNAPPY和GZIP  \n\n"
			+ "	*		BLOCKSIZE    -> 数据块大小  \n\n"
			+ "	*			1、随机查询：数据块越小，索引越大，查找性能更好。  \n\n"
			+ "	*			2、顺序查询：更好的顺序扫描，需要更大的数据块。  \n\n"
			+ "	*			3、所以在使用的时候根据业务需求来判断是随机查询需求多还是顺序查询需求多，根据具体的场景而定。  \n\n"
			+ "	*		BLOCKCACHE    -> 数据块缓存  \n\n"
			+ "	*			1、如果一张表或表里的某个列族只被顺序化扫描访问或者很少被访问，这个时候就算Get或Scan花费时间是否有点儿长，  \n\n"
			+ "	*		  你也不会很在意。在这种情况下，你可以选择关闭那些列族的缓存。  \n\n"
			+ "	*			2、如果你只是执行很多顺序化扫描，你会多次倒腾缓存，并且可能会滥用缓存把应该放进缓存获得性能提升的数据给排挤出去。  \n\n"
			+ "	*		  如果关闭缓存，不仅可以避免上述情况发生，而且还可以让出更多缓存给其他表和同一个表的其他列族使用。  \n\n"
			+ "	*		BLOOMFILTER    -> 布隆过滤器  \n\n"
			+ "	*			1、HBase中存储额外的索引层次会占用额外的空间。布隆过滤器随着它们的索引对象的数据增长而增长，所以行级布隆过滤器比  \n\n"
			+ "	*		  列标识符级布隆过滤器占用空间要少。当空间不是问题的时候，它们可以帮助你榨干系统的性能潜力。  \n\n"
			+ "	*			2、BLOOMFILTER参数的默认值是ROW，表示是行级布隆过滤器。  \n\n"
			+ "	*			3、使用行级布隆过滤器需要设置为ROW，使用列标识符级布隆过滤器需要设置为ROWCOL。  \n\n"
			+ "	*			4、行级布隆过滤器在数据块里检查特定行键是否不存在，列标识符级布隆过滤器检查行和列标识符联合体是否不存在。  \n\n"
			+ "	*			5、ROWCOL布隆过滤器的开销要高于ROW布隆过滤器。  \n\n"
			+ "	*		TTL    -> 生存时间  \n\n"
			+ "	*			1、应用系统经常需要从数据库里删除老数据，配置此项，可使数据增加生命周期，超过该配置时间的数据，将会在大合并时  \n\n"
			+ "	*		  “被删除”。（单位：秒）  \n\n"
			+ "	* @throws Exception  \n\n"
			+ "	*  \n\n"
			+ "	*	hbase.appendColumnFamily(\"student\", {'CF':'level','COMPRESSION':'SNAPPY'}::stringify,...);\n\n"
			+ "	*/")
	public void appendColumnFamily(
			@Comment(name = "tableName", value = "表名称") String tableName ,
			@Comment(name = "attrs", value = "String... 类型，具体使用看注释") String... attrs
			) throws Exception {
        Admin admin = connection.getAdmin();
        for (String attr : attrs) {
            JSONObject attrObj = JSON.parseObject(attr);
            Object cf = attrObj.get("CF");
            Object versions = attrObj.get("VERSIONS");
            Object compression = attrObj.get("COMPRESSION");
            Object blocksize = attrObj.get("BLOCKSIZE");
            Object blockcache = attrObj.get("BLOCKCACHE");
            Object bloomfilter = attrObj.get("BLOOMFILTER");
            Object ttl = attrObj.get("TTL");
            HColumnDescriptor cfBuilder = null;
            if (cf != null) {
                cfBuilder = new HColumnDescriptor(cf.toString());
            }
            if (versions != null) {
                cfBuilder.setMaxVersions(Integer.parseInt(versions.toString()));
            }
            if (compression != null) {
                if (compression.toString().equals("SNAPPY")) {
                    cfBuilder.setCompressionType(Algorithm.SNAPPY);
                } else if (compression.toString().equals("LZO")) {
                    cfBuilder.setCompressionType(Algorithm.LZO);
                } else if (compression.toString().equals("GZIP")) {
                    cfBuilder.setCompressionType(Algorithm.GZ);
                }
            }
            if (blocksize != null) {
                cfBuilder.setBlocksize(Integer.parseInt(blocksize.toString()));
            }
            if (blockcache != null) {
                if (blockcache.toString().equals("true")) {
                    cfBuilder.setBlockCacheEnabled(true);
                } else {
                    cfBuilder.setBlockCacheEnabled(false);
                }
            }
            if (bloomfilter != null) {
                if (bloomfilter.toString().equals("ROWCOL")) {
                    cfBuilder.setBloomFilterType(BloomType.ROWCOL);
                } else if (bloomfilter.toString().equals("ROW")) {
                    cfBuilder.setBloomFilterType(BloomType.ROW);
                }
            }
            if (ttl != null) {
                cfBuilder.setTimeToLive(Integer.parseInt(ttl.toString()));
            }
            admin.addColumn(TableName.valueOf(tableName), cfBuilder);
        }
        admin.close();
    }
	
	@Comment("/**删除列族  \n\n"
			+ "	* @param tableName  \n\n"
			+ "	* @param cfNames  \n\n"
			+ "	*			列族名称，数组方式添加多个  \n\n"
			+ "	* @throws Exception  \n\n"
			+ "	*  \n\n"
			+ "	*	hbase.delColumnFamily(\"student\", \"level\",\"level2\",...);\n\n"
			+ "	*/")
	public void delColumnFamily(
			@Comment(name = "tableName", value = "表名称") String tableName ,
			@Comment(name = "cfNames", value = "String... 类型，具体使用看注释") String... cfNames
		) throws Exception {
        Admin admin = connection.getAdmin();
        for (String cf : cfNames) {
            admin.deleteColumn(TableName.valueOf(tableName), Bytes.toBytes(cf.toString()));
        }
        admin.close();
    }
	
	
	@Comment("/**\n\n"
			+ "	 * 删除表\n\n"
			+ "	 * @param tableName 表名称\n\n"
			+ "	 * @throws Exception\n\n"
			+ "	 * \n\n"
			+ "	 * 	hbase.delTable(\"student\");\n\n"
			+ " 	 */")
	public void delTable(@Comment(name = "tableName", value = "表名称") String tableName) throws Exception {

        Admin admin = connection.getAdmin();
        TableName tableNameObj = TableName.valueOf(tableName);
        //删除表，先禁用表
        admin.disableTable(tableNameObj);
        admin.deleteTable(tableNameObj);
        admin.close();
    }

	
	@Comment("/**\n\n"
			+ "	 * 添加或者修改一个单元格(列)的数据\n\n"
			+ "	 * @param tableName 表名称\n\n"
			+ "	 * @param rowKey rowkey值\n\n"
			+ "	 * @param columnFamily 列族名称\n\n"
			+ "	 * @param column 列名称\n\n"
			+ "	 * @param value 列值\n\n"
			+ "	 * @throws Exception\n\n"
			+ "	 * \n\n"
			+ "	 * 		hbase.put2HBaseCell(\"student\", \"haiyang\", \"info\", \"age\", \"17\");\n\n"
			+ "	 */")
	public void put2HBaseCell(
			@Comment(name = "tableName", value = "表名称") String tableName, 
			@Comment(name = "rowKey", value = "rowkey值") String rowKey, 
			@Comment(name = "columnFamily", value = "列族名称") String columnFamily, 
			@Comment(name = "column", value = "列名称") String column, 
			@Comment(name = "value", value = " 列值") String value)
			throws Exception {
        Table table = connection.getTable(TableName.valueOf(tableName));
        Put put = new Put(Bytes.toBytes(rowKey));
        put.addColumn(Bytes.toBytes(columnFamily), Bytes.toBytes(column), Bytes.toBytes(value));
        table.put(put);
        table.close();
    }

	
	@Comment("/**\n\n"
			+ "	 * 向hbase中添加一行或多行数据\n\n"
			+ "	 * @param tableName\n\n"
			+ "	 * @param rows  行数据 \n\n"
			+ "	 * 	 {\n\n"
			+ "        \"peter\":{'info:age':'18','info:sex':'man','info:class':'B班'}::stringify,\n\n"
			+ "        \"xuhaiyang\":  {\"info:age\":'30','info:sex':'man','info:class':'A班'}::stringify\n\n"
			+ "	 *   }\n\n"
			+ "	 * @throws Exception\n\n"
			+ "	 * \n\n"
			+ "	 * 		hbase.put2HBasRows(\"student\", rows);\n\n"
			+ "	 */")
	public void put2HBasRows(
			@Comment(name = "tableName", value = "表名称") String tableName, 
			@Comment(name = "rows", value = "行数据,具体看注释") Map<String,String> rows) throws Exception {
        Table table = connection.getTable(TableName.valueOf(tableName));
        List<Put> puts = new ArrayList<>();
        for (Map.Entry<String, String> row : rows.entrySet()) {
            String rowkey = row.getKey();
            String cfInfo = row.getValue();
            //指定Rowkey，返回put对象
            Put put = new Put(Bytes.toBytes(rowkey));

            @SuppressWarnings("unchecked")
            Map<String, String> rowInfo = JSON.parseObject(cfInfo, Map.class);
            for (Map.Entry<String, String> entry : rowInfo.entrySet()) {
                String cellCFAndColName = entry.getKey();
                String val = entry.getValue();
                if (cellCFAndColName.indexOf(":") != -1) {
                    String[] cellInfo = cellCFAndColName.split(":");
                    //向put对象中指定列族、列、值
                    //put 'student','xuhaiyang','info:age','18'
                    put.addColumn(Bytes.toBytes(cellInfo[0]), Bytes.toBytes(cellInfo[1]), Bytes.toBytes(val));
                }
            }
            puts.add(put);
        }
        table.put(puts);
        table.close();
    }
	
	@Comment("/**\n\n"
			+ "     * 根据rowkey查询数据\n\n"
			+ "     * @param tableName\n\n"
			+ "     * @param rowkey\n\n"
			+ "     * 			主键值\n\n"
			+ "     * @param cloumns\n\n"
			+ "     * 			返回的列族信息：info:age,info:sex,...\n\n"
			+ "     * \n\n"
			+ "     * @return\n\n"
			+ "     * 		{\"rowkey\":\"xxxx\",\"info:sex\":\"男\",\"info:age\":\"10\",\"level:class\":\"A班级\"}\n\n"
			+ "     * \n\n"
			+ "     *  	hbase.get2HBasRowsByRowkey(\"student\", \"haiyang\",\"info:sex\",\"info:age\",\"level:class\");\n\n"
			+ "     *  	返回：{\"rowkey\":\"xxxx\",\"info:sex\":\"男\",\"info:age\":\"10\",\"level:class\":\"A班级\"}\n\n"
			+ "     * @throws IOException\n\n"
			+ "     */")
    public Map<String,String> get2HBasRowsByRowkey(
    		@Comment(name = "tableName", value = "表名称") String tableName, 
    		@Comment(name = "rowkey", value = "主键值") String rowkey, 
    		@Comment(name = "cloumns", value = "返回的列族信息：info:age,info:sex,...,可以不填写") String...cloumns) throws Exception {

        //获取Table，指定要操作的表名，表需要提前创建好
        Table table = connection.getTable(TableName.valueOf(tableName));
        //指定Rowkey，返回Get对象
        Get get = new Get(Bytes.toBytes(rowkey));
        //【可选】可以在这里指定要查询指定Rowkey数据哪些列族中的列
        //如果不指定，默认查询指定Rowkey所有列的内容
        for (String attrs : cloumns) {
            String[] clmInfo = attrs.split(":");
            if (clmInfo.length == 2) {
                get.addColumn(Bytes.toBytes(clmInfo[0]), Bytes.toBytes(clmInfo[1]));
            }
        }
        Result result = table.get(get);
        //如果不清楚HBase中到底有哪些列族和列，可以使用listCells()获取所有cell（单元格），cell对应的是某一列的数据
        List<Cell> cells = result.listCells();
        Map<String, String> datas = new HashMap<>();
        if (cells != null) {
            datas.put("rowkey", rowkey);
            for (Cell cell : cells) {
                //注意：下面获取的信息都是字节类型的，可以通过new String(bytes)转为字符串
                //列族
                byte[] famaily_bytes = CellUtil.cloneFamily(cell);
                //列
                byte[] column_bytes = CellUtil.cloneQualifier(cell);
                //值
                byte[] value_bytes = CellUtil.cloneValue(cell);
//                 System.out.println("列族："+new String(famaily_bytes)+",列："+new String(column_bytes)+",值："+new String(value_bytes));
                datas.put(new String(famaily_bytes) + ":" + new String(column_bytes), new String(value_bytes));
            }
//             System.out.println("==============================================");
            //如果明确知道HBase中有哪些列族和列，可以使用getValue(family,qualifier)直接获取指定列族中指定列的数据
//             byte[] age_bytes = result.getValue(Bytes.toBytes("info"), Bytes.toBytes("age"));
//             system.out.println("age列的值："+new String(age_bytes));
        }

        //关闭table连接
        table.close();
        return datas;
    }
    
	@Comment("/*\n\n"
			+ "     * 根据rowkey正则查询数据 \n\n"
			+ "     * @param tableName  \n\n"
			+ "     * @param rowkeyRegex  \n\n"
			+ "	    *				xu.* 	//包含xu这个字符串  \n\n"
			+ "	    *				.*xu 	//包含xu这个字符串  \n\n"
			+ "	    *				^xu 	//以xu开头的  \n\n"
			+ "	    *				xu$ 	//以xu结尾  \n\n"
			+ "     * @param cloumns  \n\n"
			+ "     *			返回的列族信息：info:age,info:sex,...  \n\n"
			+ "     * @return  \n\n"
			+ "     *		[  \n\n"
			+ "     *			{\"rowkey\":\"xxxx\",\"info:sex\":\"男\",\"info:age\":\"10\",\"level:class\":\"A班级\"}  \n\n"
			+ "     *			...  \n\n"
			+ "     *		]  \n\n"
			+ "     * @throws IOException  \n\n"
			+ "     *  \n\n"
			+ "     * 		hbase.get2HBasRowsByRowkeyRegex(\"student\", \"xu$\",\"info:sex\",\"info:age\",\"level:class\");\n\n"
			+ "    */")
    public List<Map<String,String>> get2HBasRowsByRowkeyRegex(
    		@Comment(name = "tableName", value = "表名称") String tableName, 
    		@Comment(name = "rowkeyRegex", value = "正则表达式，看注释使用") String rowkeyRegex, 
    		@Comment(name = "cloumns", value = "返回的列族信息：info:age,info:sex,...,可以不填写") String...cloumns) throws Exception {

        //获取Table，指定要操作的表名，表需要提前创建好
        Table table = connection.getTable(TableName.valueOf(tableName));
        Scan scan = new Scan();

        // 设置正则表达式作为RowKey的过滤条件
        Filter filter = new RowFilter(CompareOp.EQUAL, new RegexStringComparator(rowkeyRegex));
        scan.setFilter(filter);

        //【可选】可以在这里指定要查询指定Rowkey数据哪些列族中的列
        //如果不指定，默认查询指定Rowkey所有列的内容
        for (String attrs : cloumns) {
            String[] clmInfo = attrs.split(":");
            if (clmInfo.length == 2) {
                scan.addColumn(Bytes.toBytes(clmInfo[0]), Bytes.toBytes(clmInfo[1]));
            }
        }
        ResultScanner scanner = table.getScanner(scan);
        List<Map<String, String>> rsList = new LinkedList<Map<String, String>>();
        for (Result result : scanner) {
            //如果不清楚HBase中到底有哪些列族和列，可以使用listCells()获取所有cell（单元格），cell对应的是某一列的数据
            List<Cell> cells = result.listCells();
            Map<String, String> datas = new LinkedHashMap<>();
            datas.put("rowkey", new String(result.getRow()));
            for (Cell cell : cells) {
                //注意：下面获取的信息都是字节类型的，可以通过new String(bytes)转为字符串
                //列族
                byte[] famaily_bytes = CellUtil.cloneFamily(cell);
                //列
                byte[] column_bytes = CellUtil.cloneQualifier(cell);
                //值
                byte[] value_bytes = CellUtil.cloneValue(cell);
//                System.out.println("列族："+new String(famaily_bytes)+",列："+new String(column_bytes)+",值："+new String(value_bytes));
                datas.put(new String(famaily_bytes) + ":" + new String(column_bytes), new String(value_bytes));
            }
            rsList.add(datas);
        }
        //关闭table连接
        table.close();
        return rsList;
    
	
	}

	@Comment("/*\n\n"
			+ "    *根据rowkey的区间+rowkey字符串的正则的复合查询  \n\n"
			+ "	* @param tableName  \n\n"
			+ "	* @param startRegex  \n\n"
			+ "	*			数据的开始占位如：  \n\n"
			+ "	*				20240101*  -------表示查询rowkey >= 前缀字符串为 20240101 的数据  \n\n"
			+ "	* @param endRegex  \n\n"
			+ "	*			数据的结束占位如：  \n\n"
			+ "	*				20240105*  -------表示查询rowkey < 前缀字符串为 20240105 的数据  \n\n"
			+ "	* @param rowkeyRegex  \n\n"
			+ "	*			rowkey符合的正则表达式,如：  \n\n"
			+ "	*				xu.* //包含xu这个字符串  \n\n"
			+ "	*				*.xu //包含xu这个字符串  \n\n"
			+ "	*				^xu //以xu开头的  \n\n"
			+ "	*				xu$ //以xu结尾  \n\n"
			+ "	* @param cloumns  \n\n"
			+ "	*			返回的列族信息：info:age,info:sex,...  \n\n"
			+ "	* @return  \n\n"
			+ "	*		[  \n\n"
			+ "	*			{\"rowkey\":\"xxxx\",\"info:sex\":\"男\",\"info:age\":\"10\",\"level:class\":\"A班级\"}  \n\n"
			+ "	*			...  \n\n"
			+ "	*		]  \n\n"
			+ "	* @throws IOException  \n\n"
			+ "	*  \n\n"
			+ "	* 查询student表中：  \n\n"
			+ "	*	1、rowkey >= 20240101* && rowkey < 20240104* 的数据   \n\n"
			+ "	*   2、并且 rowkey  包含 haiyang字符串的数据  \n\n"
			+ "	*   hbase.get2HBasRowsByRowkeyRegexAndBetween(\"student\",\"20240101*\",\"20240104*\",new String[]{\".*xuhaiyang$\",\".*haiyang$\"},\"info:sex\",\"info:age\",\"level:class\");\n\n"
			+ "	*/")
    public List<Map<String,Object>> get2HBasRowsByRowkeyRegexAndBetween(
    		@Comment(name = "tableName", value = "表名称") String tableName, 
    		@Comment(name = "startRegex", value = "数据的开始占位") String startRegex, 
    		@Comment(name = "endRegex", value = "数据的结束占位") String endRegex, 
    		@Comment(name = "rowkeyRegex", value = "rowkey符合的正则表达式,[xx,xx]可以写多个") String[] rowkeyRegex, 
    		@Comment(name = "cloumns", value = "返回的列族信息：info:age,info:sex,...,可以不填写") String...cloumns) throws Exception {

        Table table = connection.getTable(TableName.valueOf(tableName));
        Scan scan = new Scan();
        scan.setStartRow(Bytes.toBytes(startRegex)).setStopRow(Bytes.toBytes(endRegex));

        if (rowkeyRegex.length == 1) {
            Filter filter = new RowFilter(CompareOp.EQUAL, new RegexStringComparator(rowkeyRegex[0]));
            scan.setFilter(filter);
        } else {
            List<Filter> filters = new ArrayList<>();
            for (String rowkey : rowkeyRegex) {
                Filter filter = new RowFilter(CompareOp.EQUAL, new RegexStringComparator(rowkey));
                filters.add(filter);
            }
            FilterList filterList = new FilterList(FilterList.Operator.MUST_PASS_ONE, filters);
            scan.setFilter(filterList);
        }

        // 指定查询的列族和列
        for (String attrs : cloumns) {
            String[] clmInfo = attrs.split(":");
            if (clmInfo.length == 2) {
                scan.addColumn(Bytes.toBytes(clmInfo[0]), Bytes.toBytes(clmInfo[1]));
            }
        }

        ResultScanner scanner = table.getScanner(scan);

        // 使用线程池并行处理
        int cpuCores = Runtime.getRuntime().availableProcessors();
        ExecutorService executor = Executors.newFixedThreadPool(cpuCores * 2);

        ConcurrentHashMap<Integer, Map<String, Object>> resultMap = new ConcurrentHashMap<>();
        List<Future<?>> futures = new ArrayList<>();

        int index = 0;
        for (Result result : scanner) {
            final int currentIndex = index++;
            futures.add(executor.submit(() -> {
                Map<String, Object> datas = new LinkedHashMap<>();
                datas.put("rowkey", new String(result.getRow()));
                for (Cell cell : result.listCells()) {
                    String family = new String(CellUtil.cloneFamily(cell));
                    String column = new String(CellUtil.cloneQualifier(cell));
                    String value = new String(CellUtil.cloneValue(cell));
                    datas.put(family + ":" + column, value);
                }
                resultMap.put(currentIndex, datas);
            }));
        }

        // 等待所有任务完成
        for (Future<?> f : futures) {
            f.get();
        }
        executor.shutdown();

        // 按顺序组装结果
        List<Map<String, Object>> rsList = new ArrayList<>();
        for (int i = 0; i < resultMap.size(); i++) {
            rsList.add(resultMap.get(i));
        }

        table.close();
        return rsList;
    }
	
	 public long getCountByRowkeyRegexAndBetween(
	    		String tableName,
	    		String startRegex,
	    		String endRegex,
	    		String[] rowkeyRegex) throws Exception {
	    	
	    	Table table = connection.getTable(TableName.valueOf(tableName));
	    	Scan scan = new Scan();
	    	scan.setStartRow(Bytes.toBytes(startRegex)).setStopRow(Bytes.toBytes(endRegex));
	    	
	    	if (rowkeyRegex.length == 1) {
	    		Filter filter = new RowFilter(CompareOp.EQUAL, new RegexStringComparator(rowkeyRegex[0]));
	    		scan.setFilter(filter);
	    	} else {
	    		List<Filter> filters = new ArrayList<>();
	    		for (String rowkey : rowkeyRegex) {
	    			Filter filter = new RowFilter(CompareOp.EQUAL, new RegexStringComparator(rowkey));
	    			filters.add(filter);
	    		}
	    		FilterList filterList = new FilterList(FilterList.Operator.MUST_PASS_ONE, filters);
	    		scan.setFilter(filterList);
	    	}
	    	 
	    	long count = 0;
	        try (ResultScanner scanner = table.getScanner(scan)) {
	            for (Result _result : scanner) {
	                count++;
	            }
	        }
	        table.close();
	        return count;
	    }
    
	@Comment("/*\n\n"
			+ "    * 删除行或行列数据 \n\n"
			+ "    * @param tableName  \n\n"
			+ "    * @param rowkeyRegex \n\n"
			+ "    *			rowey的正则过滤器（'.*T001$'） \n\n"
			+ "    * @param cells  \n\n"
			+ "    *			列族中的列名称， info:age , level:class  \n\n"
			+ "    *			删除行数据是不填写\n\n"
			+ "    * @throws IOException  \n\n"
			+ "    *  \n\n"
			+ "    * 删除所有以包含2024010的rowekey 中的 info列族 的age 数据  \n\n"
			+ "    * hbase.del2HBaseRowOrCell(\"student\",\"2024010.*\",\"info:age\",\"leval:class\");  \n\n"
			+ "    *  \n\n"
			+ "    * 删除所有以包含2024010的行数据  \n\n"
			+ "    * 	hbase.del2HBaseRowOrCell(\"student\",\"2024010*\");  \n\n"
			+ "    */")
    public void del2HBaseRowOrCell(
    		@Comment(name = "tableName", value = "表名称") String tableName, 
    		@Comment(name = "rowkeyRegex", value = "rowey的正则过滤器（'.*T001$'）") String rowkeyRegex, 
    		@Comment(name = "cells", value = "列族中的列名称， info:age , level:class ") String...cells) throws Exception {

        //获取Table，指定要操作的表名，表需要提前创建好
        Table table = connection.getTable(TableName.valueOf(tableName));

        // 创建Scan对象
        Scan scan = new Scan();
        // 设置正则表达式作为RowKey的过滤条件
        Filter filter = new RowFilter(CompareOp.EQUAL, new RegexStringComparator(rowkeyRegex));
        scan.setFilter(filter);

        // 获取匹配正则表达式的RowKey
        ResultScanner scanner = table.getScanner(scan);
        List<Delete> dels = new ArrayList<>();
        for (Result result : scanner) {
            byte[] rowKey = result.getRow();
            // 匹配则删除数据
            Delete delete = new Delete(rowKey);
            for (String cell : cells) {
                String[] cellInfo = cell.split(":");
                if (cellInfo.length == 2) {
                    delete.addColumn(Bytes.toBytes(cellInfo[0]), Bytes.toBytes(cellInfo[1]));
                }
            }
            dels.add(delete);
        }
        table.delete(dels);
        // 关闭连接
        table.close();
    }
	
	@Comment("/**\n\n"
			+ "    * 根据rowkey的区间删除数据或者数据列  \n\n"
			+ "    * @param tableName  \n\n"
			+ "    * @param startRegex  \n\n"
			+ "    *			数据的开始占位如：  \n\n"
			+ "    *				20240101*  -------表示查询rowkey >= 前缀字符串为 20240101 的数据  \n\n"
			+ "    * @param endRegex  \n\n"
			+ "    *			数据的结束占位如：  \n\n"
			+ "    *				20240105*  -------表示查询rowkey < 前缀字符串为 20240105 的数据  \n\n"
			+ "    * @param cells  \n\n"
			+ "    *			返回的列族信息：info:age,info:sex,...  \n\n"
			+ "    * @throws IOException  \n\n"
			+ "    *  \n\n"
			+ "    * 删除student表中：  \n\n"
			+ "    *	1、rowkey >= 20240101* && rowkey < 20240104* 的数据   \n\n"
			+ "    *      hbase.del2HBaseRowOrCellByArea(\"student\",\"20240101*\",\"20240104*\");  \n\n"
			+ "    *	2、rowkey >= 20240101* && rowkey < 20240104* 的数据中的指定列   \n\n"
			+ "    *\n\n"
			+ "    *   hbase.del2HBaseRowOrCellByArea(\"student\",\"20240101*\",\"20240104*\",\"info:sex\",\"info:age\",\"level:class\");\n\n"
			+ "    */")
	public void del2HBaseRowOrCellByArea(
			@Comment(name = "tableName", value = "表名称") String tableName, 
    		@Comment(name = "startRegex", value = "数据的开始占位") String startRegex, 
    		@Comment(name = "endRegex", value = "数据的结束占位") String endRegex, 
    		@Comment(name = "cells", value = "列族中的列名称， info:age , level:class ") String...cells) throws Exception {
        // 获取 HBase 表格对象
        Table table = connection.getTable(TableName.valueOf(tableName));

        // 创建 Scan 对象，设置行键的正则表达式过滤条件
        Scan scan = new Scan();
        scan.setStartRow(Bytes.toBytes(startRegex)).setStopRow(Bytes.toBytes(endRegex));

        // 获取匹配正则表达式的行键
        ResultScanner scanner = table.getScanner(scan);

        // 创建删除操作列表
        List<Delete> dels = new ArrayList<>();

        // 遍历匹配的行键，添加删除操作
        for (Result result : scanner) {
            byte[] rowKey = result.getRow();
            Delete delete = new Delete(rowKey);

            // 遍历需要删除的列族和列，添加删除操作
            for (String cell : cells) {
                String[] cellInfo = cell.split(":");
                if (cellInfo.length == 2) {
                    delete.addColumn(Bytes.toBytes(cellInfo[0]), Bytes.toBytes(cellInfo[1]));
                }
            }
            // 将删除操作添加到列表
            dels.add(delete);
        }
        // 执行删除操作
        table.delete(dels);
        // 关闭表格连接
        table.close();
    }
    
}
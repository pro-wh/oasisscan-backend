package romever.scan.oasisscan.service;

import com.alicp.jetcache.anno.CacheRefresh;
import com.alicp.jetcache.anno.CacheType;
import com.alicp.jetcache.anno.Cached;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import lombok.extern.slf4j.Slf4j;
import org.elasticsearch.ElasticsearchException;
import org.elasticsearch.action.get.GetResponse;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.client.core.CountResponse;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.rest.RestStatus;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.SearchHits;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.elasticsearch.search.sort.SortOrder;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import romever.scan.oasisscan.common.ApiResult;
import romever.scan.oasisscan.common.Constants;
import romever.scan.oasisscan.common.ESFields;
import romever.scan.oasisscan.common.ElasticsearchConfig;
import romever.scan.oasisscan.common.client.ApiClient;
import romever.scan.oasisscan.db.JestDao;
import romever.scan.oasisscan.entity.Runtime;
import romever.scan.oasisscan.entity.RuntimeStatsInfo;
import romever.scan.oasisscan.entity.RuntimeStatsType;
import romever.scan.oasisscan.entity.ValidatorInfo;
import romever.scan.oasisscan.repository.RuntimeRepository;
import romever.scan.oasisscan.repository.RuntimeStatsInfoRepository;
import romever.scan.oasisscan.repository.RuntimeStatsRepository;
import romever.scan.oasisscan.repository.ValidatorInfoRepository;
import romever.scan.oasisscan.utils.Mappers;
import romever.scan.oasisscan.utils.Texts;
import romever.scan.oasisscan.vo.chain.runtime.AbstractRuntimeTransaction;
import romever.scan.oasisscan.vo.chain.runtime.RuntimeRound;
import romever.scan.oasisscan.vo.chain.runtime.RuntimeTransaction;
import romever.scan.oasisscan.vo.chain.runtime.emerald.EmeraldTransaction;
import romever.scan.oasisscan.vo.response.*;
import romever.scan.oasisscan.vo.response.runtime.ListRuntimeTransactionResponse;
import romever.scan.oasisscan.vo.response.runtime.RuntimeTransactionResponse;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

import static romever.scan.oasisscan.common.ESFields.*;

@Slf4j
@Service
public class RuntimeService {

    @Autowired
    private ApiClient apiClient;
    @Autowired
    private RestHighLevelClient elasticsearchClient;
    @Autowired
    private ElasticsearchConfig elasticsearchConfig;
    @Autowired
    private RuntimeRepository runtimeRepository;
    @Autowired
    private RuntimeStatsRepository runtimeStatsRepository;
    @Autowired
    private RuntimeStatsInfoRepository runtimeStatsInfoRepository;
    @Autowired
    private ValidatorInfoRepository validatorInfoRepository;

    @Cached(expire = 30, cacheType = CacheType.LOCAL, timeUnit = TimeUnit.SECONDS)
    public ApiResult roundList(String runtimeId, int size, int page) {
        long total = 0;
        List<RuntimeRoundResponse> roundList = Lists.newArrayList();
        SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
        BoolQueryBuilder boolQueryBuilder = QueryBuilders.boolQuery();
        boolQueryBuilder.filter(QueryBuilders.termQuery(ESFields.RUNTIME_ROUND_NAMESPACE, runtimeId));
        searchSourceBuilder.query(boolQueryBuilder);
        searchSourceBuilder.sort(ESFields.RUNTIME_ROUND_ROUND, SortOrder.DESC);
        searchSourceBuilder.from(size * (page - 1));
        searchSourceBuilder.size(size);
        searchSourceBuilder.trackTotalHits(true);

        try {
            SearchResponse searchResponse = JestDao.search(elasticsearchClient, elasticsearchConfig.getRuntimeRoundIndex(), searchSourceBuilder);
            if (searchResponse.getTotalShards() == searchResponse.getSuccessfulShards()) {
                SearchHits hits = searchResponse.getHits();
                total = hits.getTotalHits().value;
                SearchHit[] searchHits = hits.getHits();
                for (SearchHit hit : searchHits) {
                    RuntimeRound.Header round = Mappers.parseJson(hit.getSourceAsString(), new TypeReference<RuntimeRound.Header>() {
                    });
                    if (round != null) {
                        roundList.add(RuntimeRoundResponse.of(round));
                    }
                }
            }
        } catch (IOException e) {
            log.error("error", e);
        }

        return ApiResult.page(roundList, page, size, total);
    }

    public RuntimeRoundResponse roundInfo(String runtimeId, long round) {
        RuntimeRoundResponse response = null;
        String esId = runtimeId + "_" + round;
        try {
            GetResponse getResponse = JestDao.get(elasticsearchClient, elasticsearchConfig.getRuntimeRoundIndex(), String.valueOf(esId));
            if (getResponse.isExists()) {
                RuntimeRound.Header header = Mappers.parseJson(getResponse.getSourceAsString(), new TypeReference<RuntimeRound.Header>() {
                });
                if (header != null) {
                    response = RuntimeRoundResponse.of(header);
                    long latestRound = latestRound(runtimeId);
                    response.setNext(round < latestRound);
                }
            }
        } catch (ElasticsearchException e) {
            if (e.status() == RestStatus.NOT_FOUND) {
                log.warn("runtime round {} not found.", esId);
            }
        } catch (IOException e) {
            log.error("error", e);
        }
        return response;
    }

    private long latestRound(String runtimeId) {
        long roundHeight = 0;
        SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
        BoolQueryBuilder boolQueryBuilder = QueryBuilders.boolQuery();
        boolQueryBuilder.filter(QueryBuilders.termQuery(ESFields.RUNTIME_ROUND_NAMESPACE, runtimeId));
        searchSourceBuilder.query(boolQueryBuilder);
        searchSourceBuilder.sort(ESFields.RUNTIME_ROUND_ROUND, SortOrder.DESC);
        searchSourceBuilder.size(1);
        searchSourceBuilder.trackTotalHits(true);

        try {
            SearchResponse searchResponse = JestDao.search(elasticsearchClient, elasticsearchConfig.getRuntimeRoundIndex(), searchSourceBuilder);
            if (searchResponse.getTotalShards() == searchResponse.getSuccessfulShards()) {
                SearchHits hits = searchResponse.getHits();
                SearchHit[] searchHits = hits.getHits();
                for (SearchHit hit : searchHits) {
                    RuntimeRound.Header round = Mappers.parseJson(hit.getSourceAsString(), new TypeReference<RuntimeRound.Header>() {
                    });
                    if (round != null) {
                        roundHeight = round.getRound();
                    }
                }
            }
        } catch (IOException e) {
            log.error("error", e);
        }
        return roundHeight;
    }

    @CacheRefresh(refresh = 60, timeUnit = TimeUnit.SECONDS)
    public List<RuntimeResponse> runtimeList() {
        List<RuntimeResponse> list = Lists.newArrayList();
        List<Runtime> runtimeList = runtimeRepository.findAll();
        if (!CollectionUtils.isEmpty(runtimeList)) {
            for (Runtime runtime : runtimeList) {
                RuntimeResponse response = new RuntimeResponse();
                response.setName(runtime.getName());
                response.setRuntimeId(runtime.getRuntimeId());
                list.add(response);
            }
        }
        return list;
    }

    @Cached(expire = 30, cacheType = CacheType.LOCAL, timeUnit = TimeUnit.SECONDS)
    public List<RuntimeStatsResponse> runtimeStats(String runtimeId, int sort) {
        List<RuntimeStatsResponse> responses = Lists.newArrayList();
        List<String> entities = runtimeStatsInfoRepository.entities(runtimeId);
        if (CollectionUtils.isEmpty(entities)) {
            return responses;
        }

        RuntimeStatsType[] types = RuntimeStatsType.class.getEnumConstants();
        for (String entity : entities) {
            List<RuntimeStatsInfo> statsInfoList = runtimeStatsInfoRepository.findByRuntimeIdAndEntityId(runtimeId, entity);
            if (CollectionUtils.isEmpty(statsInfoList)) {
                return responses;
            }
            RuntimeStatsResponse response = new RuntimeStatsResponse();
            response.setEntityId(entity);
            //info
            Optional<ValidatorInfo> optionalValidatorInfo = validatorInfoRepository.findByEntityId(entity);
            String address = null;
            boolean validator = false;
            if (optionalValidatorInfo.isPresent()) {
                ValidatorInfo info = optionalValidatorInfo.get();
                response.setName(info.getName());
                response.setIcon(info.getIcon());
                address = info.getEntityAddress();
                validator = true;
            }

            if (Texts.isBlank(address)) {
                address = apiClient.pubkeyToBech32Address(entity);
            }
            response.setAddress(address);
            response.setValidator(validator);

            //stats
            Map<String, Long> statsMap = Maps.newLinkedHashMap();
            for (RuntimeStatsType type : types) {
                statsMap.put(type.name().toLowerCase(), 0L);
            }
            for (RuntimeStatsInfo info : statsInfoList) {
                for (RuntimeStatsType type : types) {
                    if (type == info.getStatsType()) {
                        statsMap.put(type.name().toLowerCase(), info.getCount());
                        break;
                    }
                }
            }
            response.setStats(statsMap);
            responses.add(response);
        }
        responses.sort((r1, r2) -> {
            Map<String, Long> map1 = r1.getStats();
            Map<String, Long> map2 = r2.getStats();
            long count1 = map1.get(types[sort].name().toLowerCase());
            long count2 = map2.get(types[sort].name().toLowerCase());
            if (count1 == count2) {
                return r1.getEntityId().compareTo(r2.getEntityId());
            }
            return (int) (map2.get(types[sort].name().toLowerCase()) - map1.get(types[sort].name().toLowerCase()));
        });
        return responses;
    }

    @Cached(expire = 30, cacheType = CacheType.LOCAL, timeUnit = TimeUnit.SECONDS)
    public ApiResult runtimeTransactions(int size, int page, String runtimeId) {
        long total = 0;
        List<ListRuntimeTransactionResponse> responses = Lists.newArrayList();

        SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
        BoolQueryBuilder boolQueryBuilder = QueryBuilders.boolQuery();
        boolQueryBuilder.filter(QueryBuilders.termQuery(RUNTIME_TRANSACTION_ID, runtimeId));
        searchSourceBuilder.query(boolQueryBuilder);

        searchSourceBuilder.sort(RUNTIME_TRANSACTION_ROUND, SortOrder.DESC);
        searchSourceBuilder.from(size * (page - 1));
        searchSourceBuilder.size(size);
        searchSourceBuilder.trackTotalHits(true);

        try {
            SearchResponse searchResponse = JestDao.search(elasticsearchClient, elasticsearchConfig.getRuntimeTransactionIndex(), searchSourceBuilder);
            if (searchResponse.getTotalShards() == searchResponse.getSuccessfulShards()) {
                SearchHits hits = searchResponse.getHits();
                total = hits.getTotalHits().value;
                SearchHit[] searchHits = hits.getHits();
                for (SearchHit hit : searchHits) {
                    JsonNode jsonHit = Mappers.parseJson(hit.getSourceAsString());
                    String type = jsonHit.path("type").asText();
                    AbstractRuntimeTransaction tx;
                    if (type.equalsIgnoreCase("evm")) {
                        tx = Mappers.parseJson(hit.getSourceAsString(), new TypeReference<EmeraldTransaction>() {
                        });
                    } else {
                        tx = Mappers.parseJson(hit.getSourceAsString(), new TypeReference<RuntimeTransaction>() {
                        });
                    }

                    if (tx != null) {
                        ListRuntimeTransactionResponse response = new ListRuntimeTransactionResponse();
                        BeanUtils.copyProperties(tx, response);
                        response.setRuntimeId(tx.getRuntime_id());
                        response.setTxHash(tx.getTx_hash());
                        responses.add(response);
                    }
                }
            }
        } catch (IOException e) {
            log.error("error", e);
        }

        return ApiResult.page(responses, page, size, total);
    }

    /**
     * transaction detail
     *
     * @param runtimeId
     * @param txHash
     * @return
     */
    public RuntimeTransactionResponse transactionInfo(String runtimeId, String txHash) {
        RuntimeTransactionResponse response = null;
        String esId = runtimeId + "_" + txHash;
        try {
            GetResponse getResponse = JestDao.get(elasticsearchClient, elasticsearchConfig.getRuntimeTransactionIndex(), esId);
            if (getResponse.isExists()) {
                JsonNode jsonHit = Mappers.parseJson(getResponse.getSourceAsString());
                String type = jsonHit.path("type").asText();
                if (type.equalsIgnoreCase("evm")) {
                    EmeraldTransaction tx = Mappers.parseJson(getResponse.getSourceAsString(), new TypeReference<EmeraldTransaction>() {
                    });
                    if (tx != null) {
                        response = new RuntimeTransactionResponse();
                        BeanUtils.copyProperties(tx, response);
                        response.setRuntimeId(tx.getRuntime_id());
                        response.setTxHash(tx.getTx_hash());
                        RuntimeTransactionResponse.Ethereum etx = new RuntimeTransactionResponse.Ethereum();
                        BeanUtils.copyProperties(tx, etx);
                        response.setEtx(etx);
                    }
                } else {
                    RuntimeTransaction tx = Mappers.parseJson(getResponse.getSourceAsString(), new TypeReference<RuntimeTransaction>() {
                    });
                    if (tx != null) {
                        response = new RuntimeTransactionResponse();
                        BeanUtils.copyProperties(tx, response);
                        response.setRuntimeId(tx.getRuntime_id());
                        response.setTxHash(tx.getTx_hash());
                        List<RuntimeTransaction.Si> sis = tx.getAi().getSi();
                        if (!CollectionUtils.isEmpty(sis)) {
                            RuntimeTransactionResponse.Consensus ctx = new RuntimeTransactionResponse.Consensus();
                            ctx.setFrom(sis.get(0).getAddress_spec().getSignature().getAddress());
                            ctx.setTo(tx.getCall().getBody().getTo());
                            ctx.setMethod(tx.getCall().getMethod());
                            List<String> amounts = tx.getCall().getBody().getAmount();
                            if (!CollectionUtils.isEmpty(amounts)) {
                                String amount = amounts.get(0);
                                if (Texts.isNotBlank(amount)) {
                                    ctx.setAmount(Texts.formatDecimals(String.valueOf(Texts.numberFromBase64(amount)), Constants.EMERALD_DECIMALS, Constants.EMERALD_DECIMALS));
                                }
                            }
                            response.setCtx(ctx);
                        }
                    }
                }
            }
        } catch (ElasticsearchException e) {
            if (e.status() == RestStatus.NOT_FOUND) {
                log.warn("transaction {} not found.", esId);
            }
        } catch (IOException e) {
            log.error("error", e);
        }
        return response;
    }

    @Cached(expire = 15, cacheType = CacheType.LOCAL, timeUnit = TimeUnit.SECONDS)
    public RuntimeTransactionResponse transactionInfo(String txHash) {
        RuntimeTransactionResponse response = null;
        try {
            SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
            BoolQueryBuilder boolQueryBuilder = QueryBuilders.boolQuery();
            boolQueryBuilder.filter(QueryBuilders.termQuery(RUNTIME_TRANSACTION_TX_HASH, txHash));
            searchSourceBuilder.query(boolQueryBuilder);
            SearchResponse searchResponse = JestDao.search(elasticsearchClient, elasticsearchConfig.getRuntimeTransactionIndex(), searchSourceBuilder);
            if (searchResponse.getTotalShards() == searchResponse.getSuccessfulShards()) {
                SearchHits hits = searchResponse.getHits();
                SearchHit[] searchHits = hits.getHits();
                for (SearchHit hit : searchHits) {
                    JsonNode jsonHit = Mappers.parseJson(hit.getSourceAsString());
                    String type = jsonHit.path("type").asText();
                    AbstractRuntimeTransaction tx;
                    if (type.equalsIgnoreCase("evm")) {
                        tx = Mappers.parseJson(hit.getSourceAsString(), new TypeReference<EmeraldTransaction>() {
                        });
                    } else {
                        tx = Mappers.parseJson(hit.getSourceAsString(), new TypeReference<RuntimeTransaction>() {
                        });
                    }

                    if (tx != null) {
                        response = new RuntimeTransactionResponse();
                        BeanUtils.copyProperties(tx, response);
                        response.setRuntimeId(tx.getRuntime_id());
                        response.setTxHash(tx.getTx_hash());
                    }
                }
            }
        } catch (IOException e) {
            log.error("error", e);
        }
        return response;
    }

}

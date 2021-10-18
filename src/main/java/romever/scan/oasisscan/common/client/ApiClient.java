package romever.scan.oasisscan.common.client;

import com.alibaba.fastjson.JSONObject;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.cbor.CBORFactory;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.codec.digest.DigestUtils;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import romever.scan.oasisscan.utils.Mappers;
import romever.scan.oasisscan.utils.Texts;
import romever.scan.oasisscan.utils.okhttp.OkHttp;
import romever.scan.oasisscan.vo.chain.*;
import romever.scan.oasisscan.vo.chain.Runtime;

import java.io.IOException;
import java.security.MessageDigest;
import java.security.Security;
import java.util.Arrays;
import java.util.Base64;
import java.util.List;
import java.util.Map;

/**
 * @author li
 */
@Slf4j
@AllArgsConstructor
public class ApiClient {

    private String api;
    private String name;

    public Block block(Long height) {
        Block block = null;
        String url = String.format("%s/api/consensus/block/", api);
        Map<String, Object> params = Maps.newHashMap();
        params.put("name", this.name);
        if (height != null) {
            params.put("height", height);
        }
        Result<Block> result = OkHttp.of(url).queries(params).exec(new TypeReference<Result<Block>>() {
        });
        if (result != null) {
            block = result.getResult();
        }
        return block;
    }

    public Long getCurHeight() {
        Long curHeight = null;
        Block block = block(null);
        if (block != null) {
            curHeight = block.getHeight();
        }
        return curHeight;
    }

    public List<String> transactions(long height) {
        List<String> transactions = null;
        String url = String.format("%s/api/consensus/transactions/", api);
        Map<String, Object> params = Maps.newHashMap();
        params.put("name", this.name);
        params.put("height", height);
        Result<List<String>> result = OkHttp.of(url).queries(params).exec(new TypeReference<Result<List<String>>>() {
        });
        if (result != null) {
            transactions = result.getResult();
        }
        return transactions;
    }

    public TransactionWithResult transactionswithresults(long height) {
        TransactionWithResult transactionWithResult = null;
        String url = String.format("%s/api/consensus/transactionswithresults/", api);
        Map<String, Object> params = Maps.newHashMap();
        params.put("name", this.name);
        params.put("height", height);
        Result<TransactionWithResult> result = OkHttp.of(url).queries(params).exec(new TypeReference<Result<TransactionWithResult>>() {
        });
        if (result != null) {
            transactionWithResult = result.getResult();
        }
        return transactionWithResult;
    }

    public long epoch(Long height) {
        long epoch = 0;
        String url = String.format("%s/api/consensus/epoch/", api);
        Map<String, Object> params = Maps.newHashMap();
        params.put("name", this.name);
        if (height != null) {
            params.put("height", height);
        }
        Result<Long> result = OkHttp.of(url).queries(params).exec(new TypeReference<Result<Long>>() {
        });
        if (result != null && result.getResult() != null) {
            epoch = result.getResult();
        }
        return epoch;
    }

    public AccountInfo accountInfo(String address, Long height) {
        AccountInfo accountInfo = null;
        String url = String.format("%s/api/staking/accountinfo/", api);
        Map<String, Object> params = Maps.newHashMap();
        params.put("name", this.name);
        params.put("ownerKey", Texts.urlEncode(address));
        if (height != null) {
            params.put("height", height);
        }
        Result<AccountInfo> result = OkHttp.of(url).queries(params).exec(new TypeReference<Result<AccountInfo>>() {
        });
        if (result != null) {
            accountInfo = result.getResult();
        }
        return accountInfo;
    }

    public String pubkeyToBech32Address(String pubKey) {
        String address = null;
        String url = String.format("%s/api/consensus/pubkeybech32address/", api);
        Map<String, Object> params = Maps.newHashMap();
        params.put("consensus_public_key", Texts.urlEncode(pubKey));
        Result<String> result = OkHttp.of(url).queries(params).exec(new TypeReference<Result<String>>() {
        });
        if (result != null) {
            address = result.getResult();
        }
        return address;
    }

    public String base64ToBech32Address(String base64) {
        String address = null;
        String url = String.format("%s/api/consensus/base64bech32address/", api);
        Map<String, Object> params = Maps.newHashMap();
        params.put("address", Texts.urlEncode(base64));
        Result<String> result = OkHttp.of(url).queries(params).exec(new TypeReference<Result<String>>() {
        });
        if (result != null) {
            address = result.getResult();
        }
        return address;
    }

    public String pubkeyToTendermintAddress(String consensusId) {
        String address = null;
        String url = String.format("%s/api/consensus/pubkeyaddress/", api);
        Map<String, Object> params = Maps.newHashMap();
        params.put("consensus_public_key", Texts.urlEncode(consensusId));
        Result<String> result = OkHttp.of(url).queries(params).exec(new TypeReference<Result<String>>() {
        });
        if (result != null) {
            address = result.getResult();
        }
        return address;
    }

    public Map<String, Delegations> delegations(String address, Long height) {
        Map<String, Delegations> delegations = null;
        String url = String.format("%s/api/staking/delegations/", api);
        Map<String, Object> params = Maps.newHashMap();
        params.put("name", this.name);
        params.put("ownerKey", Texts.urlEncode(address));
        if (height != null) {
            params.put("height", height);
        }
        Result<Map<String, Delegations>> result = OkHttp.of(url).queries(params).exec(new TypeReference<Result<Map<String, Delegations>>>() {
        });
        if (result != null) {
            delegations = result.getResult();
        }
        return delegations;
    }

    public StakingGenesis stakingGenesis(Long height) {
        StakingGenesis stakingGenesis = null;
        String url = String.format("%s/api/staking/genesis/", api);
        Map<String, Object> params = Maps.newHashMap();
        params.put("name", this.name);
        if (height != null) {
            params.put("height", height);
        }
        Result<StakingGenesis> result = OkHttp.of(url).queries(params).exec(new TypeReference<Result<StakingGenesis>>() {
        });
        if (result != null) {
            stakingGenesis = result.getResult();
        }
        return stakingGenesis;
    }

    public RegistryGenesis registryGenesis(Long height) {
        RegistryGenesis registryGenesis = null;
        String url = String.format("%s/api/registry/genesis/", api);
        Map<String, Object> params = Maps.newHashMap();
        params.put("name", this.name);
        if (height != null) {
            params.put("height", height);
        }
        Result<RegistryGenesis> result = OkHttp.of(url).queries(params).exec(new TypeReference<Result<RegistryGenesis>>() {
        });
        if (result != null) {
            registryGenesis = result.getResult();
        }
        return registryGenesis;
    }

    public List<Node> registryNodes(Long height) {
        List<Node> nodes = null;
        String url = String.format("%s/api/registry/nodes/", api);
        Map<String, Object> params = Maps.newHashMap();
        params.put("name", this.name);
        if (height != null) {
            params.put("height", height);
        }
        Result<List<Node>> result = OkHttp.of(url).queries(params).exec(new TypeReference<Result<List<Node>>>() {
        });
        if (result != null) {
            nodes = result.getResult();
        }
        return nodes;
    }

    public List<String> accounts(Long height) {
        List<String> accounts = null;
        String url = String.format("%s/api/staking/accounts/", api);
        Map<String, Object> params = Maps.newHashMap();
        params.put("name", this.name);
        if (height != null) {
            params.put("height", height);
        }
        Result<List<String>> result = OkHttp.of(url).queries(params).exec(new TypeReference<Result<List<String>>>() {
        });
        if (result != null) {
            accounts = result.getResult();
        }
        return accounts;
    }

    public List<SchedulerValidator> schedulerValidators(Long height) {
        List<SchedulerValidator> validators = Lists.newArrayList();
        String url = String.format("%s/api/scheduler/validators/", api);
        Map<String, Object> params = Maps.newHashMap();
        params.put("name", this.name);
        if (height != null) {
            params.put("height", height);
        }
        Result<List<SchedulerValidator>> result = OkHttp.of(url).queries(params).exec(new TypeReference<Result<List<SchedulerValidator>>>() {
        });
        if (result != null) {
            validators = result.getResult();
        }
        return validators;
    }

    public List<Runtime> runtimes(Long height) {
        List<Runtime> runtimes = Lists.newArrayList();
        String url = String.format("%s/api/registry/runtimes/", api);
        Map<String, Object> params = Maps.newHashMap();
        params.put("name", this.name);
        if (height != null) {
            params.put("height", height);
        }
        Result<List<Runtime>> result = OkHttp.of(url).queries(params).exec(new TypeReference<Result<List<Runtime>>>() {
        });
        if (result != null) {
            runtimes = result.getResult();
        }
        return runtimes;
    }

    public RuntimeRound roothashLatestblock(String namespace, Long height) {
        String url = String.format("%s/api/roothash/latestblock/", api);
        Map<String, Object> params = Maps.newHashMap();
        params.put("name", this.name);
        params.put("namespace", namespace);
        if (height != null) {
            params.put("height", height);
        }
        Result<RuntimeRound> result = OkHttp.of(url).queries(params).exec(new TypeReference<Result<RuntimeRound>>() {
        });
        if (result != null) {
            return result.getResult();
        }
        return null;
    }

    public RuntimeState roothashRuntimeState(String namespace, Long height) {
        String url = String.format("%s/api/roothash/runtimestate/", api);
        Map<String, Object> params = Maps.newHashMap();
        params.put("name", this.name);
        params.put("namespace", namespace);
        if (height != null) {
            params.put("height", height);
        }
        Result<RuntimeState> result = OkHttp.of(url).queries(params).exec(new TypeReference<Result<RuntimeState>>() {
        });
        if (result != null) {
            return result.getResult();
        }
        return null;
    }

    public List<RoothashEvent> roothashEvents(Long height) {
        String url = String.format("%s/api/roothash/events/", api);
        Map<String, Object> params = Maps.newHashMap();
        params.put("name", this.name);
        if (height != null) {
            params.put("height", height);
        }
        Result<List<RoothashEvent>> result = OkHttp.of(url).queries(params).exec(new TypeReference<Result<List<RoothashEvent>>>() {
        });
        if (result != null) {
            return result.getResult();
        }
        return null;
    }

    public static void main(String[] args) throws IOException {
        System.out.println(Texts.base64ToHex("Y2VJnoFJ21uoPCJnr21PshnxcgkUXNyc8Quou/74vpeqqJ/lXnPM1LlzfR4BBTMWboxgeVafhmg118Fc4h1TcuIggNhbzNy5dA/qx55X0W0NZUEB0rL7X6w5Q11nKOaSLnOVnK16uMwNn17Xy/HbaCGuHpHiYVQmH3f/K4JJbkA="));
//        CBORFactory f = new CBORFactory();
//        ObjectMapper mapper = new ObjectMapper(f);
//        byte[] decodedBytes = Base64.getDecoder().decode("ok03EoPvJkXdt+qlD0RG5Qxk8GQ=");
//        System.out.println(Texts.toHex(decodedBytes));
//
//        System.out.println(Texts.urlEncode("GHy8F8+OZ4QT59caArS7oK3QVH4teRdst39DfmFizgo="));
//
//        byte[] b = Texts.hexStringToByteArray("A2153069BC2FA61EAF7831DA38361565AEB21B69");
//        System.out.println(Texts.base64Encode(b));
//        byte[] c = Arrays.copyOf(b, 32);
//        System.out.println(Texts.toHex(c));

//        String raw = "omlzaWduYXR1cmWiaXNpZ25hdHVyZVhADvUz1NpOMlEVaN/TLbcXEJTaKIqUqR9E1TU/Nic2iP9wYiTfdNGOYOWb43NwtTqLU/mxOOk025UPCNy94J8RDGpwdWJsaWNfa2V5WCAJXOoUgAMPYR3SSEYE1/hG+Hu9I22svVQtORZhCzMOl3N1bnRydXN0ZWRfcmF3X3ZhbHVlWHSkY2ZlZaJjZ2FzGQPoZmFtb3VudEIH0GRib2R5omd4ZmVyX3RvWCBJ5YwRqKlGH+0gicWzsnkr6DIIsO5o15obKVLUnXvPGmt4ZmVyX3Rva2Vuc0FkZW5vbmNlD2ZtZXRob2Rwc3Rha2luZy5UcmFuc2Zlcg==";
        String raw = "omlzaWduYXR1cmWiaXNpZ25hdHVyZVhACjNmjiem1rGzBN5EIUAAK8FEnOwychQQ3ucbXjTQwAynUq/DlFW6eN4sw8wLPMaFoHwJ3s9Qd9DS7V0TEnYrA2pwdWJsaWNfa2V5WCDh4zZJ2Oe2QYYfQOqGWgbwcmw81KgR5gcUAK45X72pt3N1bnRydXN0ZWRfcmF3X3ZhbHVlWQPMpGNmZWWiY2dhcxkLuGZhbW91bnRAZGJvZHmtYXYCYmlkWCAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAByyCFeYNW8p2RraW5kAWdnZW5lc2lzpGVyb3VuZABlc3RhdGX2anN0YXRlX3Jvb3RYIMZyuNHvVu0oq4fDYixRFAab3TrXuPlzdJjQwB7O8JZ6cHN0b3JhZ2VfcmVjZWlwdHP2Z3N0b3JhZ2Wnamdyb3VwX3NpemUDbW1heF9hcHBseV9vcHMCc2NoZWNrcG9pbnRfaW50ZXJ2YWwYZHNjaGVja3BvaW50X251bV9rZXB0AnVjaGVja3BvaW50X2NodW5rX3NpemUaAIAAAHVtaW5fd3JpdGVfcmVwbGljYXRpb24CeBttYXhfYXBwbHlfd3JpdGVfbG9nX2VudHJpZXMZJxBoZXhlY3V0b3Klamdyb3VwX3NpemUDbG1heF9tZXNzYWdlcxkBAG1yb3VuZF90aW1lb3V0BXFncm91cF9iYWNrdXBfc2l6ZQFyYWxsb3dlZF9zdHJhZ2dsZXJzAWh2ZXJzaW9uc6FndmVyc2lvbqFlbWlub3IBaWVudGl0eV9pZFgg4eM2SdjntkGGH0DqhloG8HJsPNSoEeYHFACuOV+9qbdrY29uc3RyYWludHOiAaIBomltYXhfbm9kZXOhZWxpbWl0AW1taW5fcG9vbF9zaXploWVsaW1pdAMComltYXhfbm9kZXOhZWxpbWl0AW1taW5fcG9vbF9zaXploWVsaW1pdAMCoQGiaW1heF9ub2Rlc6FlbGltaXQBbW1pbl9wb29sX3NpemWhZWxpbWl0A2x0ZWVfaGFyZHdhcmUAbXR4bl9zY2hlZHVsZXKlaWFsZ29yaXRobWZzaW1wbGVubWF4X2JhdGNoX3NpemUYZHNiYXRjaF9mbHVzaF90aW1lb3V0GjuaygB0bWF4X2JhdGNoX3NpemVfYnl0ZXMaABAAAHVwcm9wb3NlX2JhdGNoX3RpbWVvdXQCcGFkbWlzc2lvbl9wb2xpY3mhcGVudGl0eV93aGl0ZWxpc3ShaGVudGl0aWVzo1ggQTg+ZjubD/36egwByAIGC6lMVBKgqo7xnZPgHVoIKzegWCDTmOyOf+IJAYMwvgAkjClxjYSaIEAw0MtJaTjHpz7VyqBYIOHjNknY57ZBhh9A6oZaBvBybDzUqBHmBxQArjlfvam3oHBnb3Zlcm5hbmNlX21vZGVsAWVub25jZRgZZm1ldGhvZHgYcmVnaXN0cnkuUmVnaXN0ZXJSdW50aW1l";
//        System.out.println(getSHA512_256(Texts.base64Decode(raw)));
//
//        JSONObject json1 = cborToJson(raw);
//        System.out.println(json1);
//        byte[] bytes = json1.getBytes("untrusted_raw_value");
//
//        JSONObject json2 = mapper.readValue(bytes, new TypeReference<JSONObject>() {
//        });
//        System.out.println(json2);
//        byte[] bytes2 = json2.getBytes("untrusted_raw_value");
//        JSONObject json3 = mapper.readValue(bytes2, new TypeReference<JSONObject>() {
//        });
//        System.out.println(json3);


        long t1 = System.currentTimeMillis();
        String txHash = Texts.sha512_256(Texts.base64Decode(raw));
        long t2 = System.currentTimeMillis();
        System.out.println(txHash + "   " + (t2 - t1));
        JSONObject txJson = Mappers.parseCborFromBase64(raw, new TypeReference<JSONObject>() {
        });
        System.out.println(txJson);
        JSONObject txJson2 = Mappers.parseCbor(txJson.getBytes("untrusted_raw_value"), new TypeReference<JSONObject>() {
        });
        System.out.println(txJson2);
        Transaction tx = Mappers.parseCbor(txJson.getBytes("untrusted_raw_value"), new TypeReference<Transaction>() {
        });
        System.out.println(Mappers.json(tx));
        if (tx != null) {
            Transaction.Body body = tx.getBody();
            if (body != null) {
                String bodyRawData = tx.getBody().getUntrusted_raw_value();
                body.setUntrusted_raw_value(null);
                JSONObject nodeJson = Mappers.parseCborFromBase64(bodyRawData, new TypeReference<JSONObject>() {
                });
                System.out.println(nodeJson);
                Node node = Mappers.parseCborFromBase64(bodyRawData, new TypeReference<Node>() {
                });
                System.out.println(Mappers.json(node));
            }
        }
//
//
//        String cbor = "ZA==";
//        byte[] decodedBytes = Base64.getDecoder().decode(cbor);
//        String hex = Texts.toHex(decodedBytes);
//        System.out.println(new String(decodedBytes, "UTF-8"));
//        System.out.println(hex);
//        System.out.println(Long.parseLong(hex, 16));
//
//        System.out.println(Texts.toHex(Base64.getDecoder().decode("L6D3C0jckcSiUDLxgFYNPJezWfnwT9eQ1zvvC4P6/HA=")));

//        String res = Mappers.parseCbor(decodedBytes, new TypeReference<String>() {
//        });
////
//        System.out.println(res);
//        run();

    }

    public static String getSHA512_256(byte[] bytes) {
        Security.addProvider(new BouncyCastleProvider());
        MessageDigest messageDigest;
        String encodestr = "";
        messageDigest = DigestUtils.getSha512_256Digest();
        messageDigest.update(bytes);
        encodestr = byte2Hex(messageDigest.digest());
        return encodestr;
    }

    private static String byte2Hex(byte[] bytes) {
        StringBuffer stringBuffer = new StringBuffer();
        String temp = null;
        for (int i = 0; i < bytes.length; i++) {
            temp = Integer.toHexString(bytes[i] & 0xFF);
            if (temp.length() == 1) {
                //1得到一位的进行补0操作
                stringBuffer.append("0");
            }
            stringBuffer.append(temp);
        }
        return stringBuffer.toString();
    }


    private static JSONObject cborToJson(String s) throws IOException {
        byte[] decodedBytes = Base64.getDecoder().decode(s);
        CBORFactory f = new CBORFactory();
        ObjectMapper mapper = new ObjectMapper(f);
        JSONObject json = mapper.readValue(decodedBytes, new TypeReference<JSONObject>() {
        });
        return json;
    }
}

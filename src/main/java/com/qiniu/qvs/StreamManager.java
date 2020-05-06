package com.qiniu.qvs;

import com.qiniu.common.QiniuException;
import com.qiniu.qvs.model.DynamicLiveRoute;
import com.qiniu.qvs.model.PatchOperation;
import com.qiniu.qvs.model.StaticLiveRoute;
import com.qiniu.qvs.model.Stream;
import com.qiniu.http.Client;
import com.qiniu.http.Response;
import com.qiniu.util.Auth;
import com.qiniu.util.StringMap;
import com.qiniu.util.UrlUtils;

public class StreamManager {

    private final String apiServer;
    private final Client client;
    private final Auth auth;

    public StreamManager(Auth auth) {
        this(auth, "http://10.200.20.26:6275");
    }

    public StreamManager(Auth auth, String apiServer) {
        this(auth, apiServer, new Client());
    }

    public StreamManager(Auth auth, String apiServer, Client client) {
        this.auth = auth;
        this.apiServer = apiServer;
        this.client = client;
    }

    /*
        创建流API

        请求参数Body:
        streamId	是	string	流名称, 流名称在空间中唯一，可包含 字母、数字、中划线、下划线；1 ~ 100 个字符长；创建后将不可修改
        desc	否	string	关于流的描述信息
        recordTemplateId	否	string	录制模版ID，配置流维度的录制模板
        snapshotTemplateId	否	string	截图模版ID，配置流维度的截图模板

        /v1/namespaces/{namespaceId}/streams
    */
    public Response createStream(String namespaceId, Stream stream) throws QiniuException {
        StringMap params = new StringMap();
        params.putNotNull("streamId", stream.getStreamID());
        params.putNotNull("desc", stream.getDesc());
        params.putNotNull("recordTemplateId", stream.getRecordTemplateId());
        params.putNotNull("snapshotTemplateId", stream.getSnapShotTemplateId());

        String url = String.format("%s/v1/namespaces/%s/streams", apiServer, namespaceId);
        return QvsResponse.post(url, params, client, auth);
    }

    /*
       删除流API
   */
    public Response deleteStream(String namespaceId, String streamId) throws QiniuException {
        String url = String.format("%s/v1/namespaces/%s/streams/%s", apiServer, namespaceId, streamId);
        return QvsResponse.delete(url, client, auth);
    }

    /*
	    更新流API
    */
    public Response updateStream(String namespaceId, String streamId, PatchOperation[] patchOperation) throws QiniuException {
        StringMap params = new StringMap().put("operations", patchOperation);
        String url = String.format("%s/v1/namespaces/%s/streams/%s", apiServer, namespaceId, streamId);
        return QvsResponse.patch(url, params, client, auth);
    }

    /*
	    查询流信息API
    */
    public Response queryStream(String namespaceId, String streamId) throws QiniuException {
        String url = String.format("%s/v1/namespaces/%s/streams/%s", apiServer, namespaceId, streamId);
        return QvsResponse.get(url, client, auth);
    }

    /*
	    获取流列表
    */
    public Response listStream(String namespaceId, int offset, int line, int qtype, String prefix, String sortBy) throws QiniuException {
        String requestUrl = String.format("%s/v1/namespaces/%s/streams", apiServer, namespaceId);
        StringMap map = new StringMap().put("offset", offset).
                put("line", line).put("qtype", qtype).
                putNotEmpty("prefix", prefix).putNotEmpty("sortBy", sortBy);
        requestUrl = UrlUtils.composeUrlWithQueries(requestUrl, map);
        return QvsResponse.get(requestUrl, client, auth);
    }

    /*
        静态模式获取流地址
     */
    public Response StaticPublishPlayURL(String namespaceId, String streamId, StaticLiveRoute staticLiveRoute) throws QiniuException {
        StringMap params = new StringMap().put("Domain",staticLiveRoute.getDomain()).
                put("DomainType",staticLiveRoute.getDomainType()).putNotNull("UrlExpireSec",staticLiveRoute.getUrlExpireSec());
        String url = String.format("%s/v1/namespaces/%s/streams/%s/domain", apiServer, namespaceId, streamId);
        return QvsResponse.post(url, params, client, auth);
    }

    /*
        动态模式获取流地址
     */
    public Response DynamicPublishPlayURL(String namespaceId, String streamId, DynamicLiveRoute dynamicLiveRoute) throws QiniuException {
        StringMap params = new StringMap();
        params.put("PublishIP", dynamicLiveRoute.getPublishIP());
        params.put("PlayIP", dynamicLiveRoute.getPlayIP());
        params.putNotNull("UrlExpireSec", dynamicLiveRoute.getUrlExpireSec());

        String url = String.format("%s/v1/namespaces/%s/streams/%s/urls", apiServer, namespaceId, streamId);
        return QvsResponse.post(url, params, client, auth);
    }

    /*
	    禁用流API
    */
    public Response disableStream(String namespaceId, String streamId) throws QiniuException {
        String url = String.format("%s/v1/namespaces/%s/streams/%s/disabled", apiServer, namespaceId, streamId);
        return QvsResponse.post(url, null, client, auth);
    }

    /*
	    启用流API
    */
    public Response enableStream(String namespaceId, String streamId) throws QiniuException {
        String url = String.format("%s/v1/namespaces/%s/streams/%s/enabled", apiServer, namespaceId, streamId);
        return QvsResponse.post(url, null, client, auth);
    }

    // 查询推流历史记录
    public Response queryStreamPubHistories(String namespaceId, String streamId, int start, int end, int offset, int line) throws QiniuException {
        if (start <= 0 || end < 0 || (start >= end && end != 0)) {
            throw new QiniuException(new IllegalArgumentException("bad argument" + start + "," + end));
        }
        String requestUrl = String.format("%s/v1/namespaces/%s/streams/%s/pubhistories", apiServer, namespaceId, streamId);
        StringMap map = new StringMap().put("start", start).put("end", end).
                putNotNull("offset", offset).putNotNull("line", line);
        requestUrl = UrlUtils.composeUrlWithQueries(requestUrl, map);
        return QvsResponse.get(requestUrl, client, auth);
    }

    // 查询录制记录
    public Response QueryStreamRecordHistories(String namespaceId, String streamId, int start, int end, int line, String marker) throws QiniuException {
        if (start <= 0 || end < 0 || (start >= end && end != 0)) {
            throw new QiniuException(new IllegalArgumentException("bad argument" + start + "," + end));
        }
        String requestUrl = String.format("%s/v1/namespaces/%s/streams/%s/recordhistories", apiServer, namespaceId, streamId);
        StringMap map = new StringMap().put("start", start).put("end", end)
                .putNotNull("line", line).putNotEmpty("marker", marker);
        requestUrl = UrlUtils.composeUrlWithQueries(requestUrl, map);
        return QvsResponse.get(requestUrl, client, auth);
    }

    // 查询流封面
    public Response QueryStreamCover(String namespaceId, String streamId) throws QiniuException{
        String url = String.format("%s/v1/namespaces/%s/streams/%s/cover", apiServer, namespaceId, streamId);
        return QvsResponse.get(url, client, auth);
    }

    // 获取截图列表
    public Response streamsSnapshots(String namespaceId, String streamId, int start, int end, int type, int line, String marker) throws QiniuException {
        if (start <= 0 || end < 0 || (start >= end && end != 0)) {
            throw new QiniuException(new IllegalArgumentException("bad argument" + start + "," + end));
        }
        String requestUrl = String.format("%s/v1/namespaces/%s/streams/%s/snapshots", apiServer, namespaceId, streamId);
        StringMap map = new StringMap().put("start", start).put("end", end)
                .putNotNull("line", line).putNotEmpty("marker", marker).put("type", type);
        requestUrl = UrlUtils.composeUrlWithQueries(requestUrl, map);
        return QvsResponse.get(requestUrl, client, auth);
    }
}

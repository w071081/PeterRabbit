package com.peterliu.peterrabbit.protocol.http;

import com.peterliu.peterrabbit.channel.TaskData;
import com.peterliu.peterrabbit.protocol.*;
import com.peterliu.peterrabbit.utils.StringUtils;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;

/**
 * Created by bavatinolab on 17/1/29.
 */
public class HttpProtocolHandler extends ProtocolHandlerAdapter {



    @Override
    public boolean detectType(TaskData taskData) {
        String firstLineStr = getFirstLine(taskData.getLoad());
        if(StringUtils.isNotBlank(firstLineStr) && firstLineStr.contains("HTTP")){
            return true;
        }
        return false;
    }

    @Override
    public ProtocolType getType() {
        return ProtocolType.http;
    }

    @Override
    public HttpRequest getRequest(TaskData taskData) {
        HttpRequest request = new HttpRequest();
        Context context = Context.getCurrentContext();
        String loaderStr = context.getLoaderStr();
        Matcher matcher = linePattern.matcher(loaderStr);
        List<String> strings = new ArrayList<String>();
        while(matcher.find()){
            strings.add(matcher.group(1));
        }
        if(strings.size() > 0){
            String[] temp = strings.get(0).split(" ");
            request.setMethod(temp[0]);
            request.setVersion(temp[2].split("/")[1]);
            temp = temp[1].split("\\?");
            request.setUrl(temp[0]);
            Map<String, String> params = new HashMap<String, String>();
            if(temp.length >1 && StringUtils.isNotBlank(temp[1])){
                for(String t : temp[1].split("&")){
                    if(StringUtils.isNotBlank(t)){
                        String[] temp2 = t.split("=");
                        params.put(temp2[0], temp2[1]);
                    }
                }
            }
            request.setParams(params);
            Map<String, String> headers = new HashMap<String, String>();
            for(int i = 1; i< strings.size() ; i++){
                if("".equals(strings.get(i))){
                    break;
                }
                String str = strings.get(i);
                int k = str.indexOf(":");
                if(k > 0 && k != str.length() -1) {
                    headers.put(str.substring(0, k), str.substring(k + 1).trim());
                }
            }
//            //判断为断点续传下载文件
//            String range = request.getHeaders().get("Range");
//            String[] ranges = range.split("-");
            request.setHeaders(headers);
        }
        return request;
    }

    @Override
    public <K extends Response, T extends Request> K getResponse(TaskData taskData, T request, String content) {
        HttpResponse httpResponse = new HttpResponse();
        httpResponse.setRequest(request);
        httpResponse.setContent(content);
        if(content == null){
            httpResponse.setStatusCode(HttpResponse.ResponseCode.NOT_FOUND);
        }else{
            httpResponse.setStatusCode(HttpResponse.ResponseCode.OK);
        }
        return (K) httpResponse;
    }

    @Override
    public <K extends Response, T extends Request> K getResponse(TaskData taskData, T request, ByteBuffer content) {
        HttpResponse httpResponse = new HttpResponse();
        httpResponse.setRequest(request);
        httpResponse.setContentBuffer(content);
        if(content == null){
            httpResponse.setStatusCode(HttpResponse.ResponseCode.NOT_FOUND);
        }else{
            httpResponse.setStatusCode(HttpResponse.ResponseCode.OK);
        }
        return (K) httpResponse;
    }


}

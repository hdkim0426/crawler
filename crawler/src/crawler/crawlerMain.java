package crawler;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
 
import org.apache.http.NameValuePair;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.utils.URLEncodedUtils;
import org.apache.http.impl.client.BasicResponseHandler;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.message.BasicNameValuePair;






public class crawlerMain {
	private static CloseableHttpClient client;
	 
    /**
     * @param args
     */
    public static void main(String[] args) {
        client = HttpClientBuilder.create().build();
 
        String url = "http://www.epeople.go.kr/jsp/user/pc/policy/KsUPcUnionPolicyList.jsp";
        Map<String, String> param = new HashMap<String, String>();
        param.put("userId", "id");
        param.put("password", "pw");
 
        crawlerMain jm = new crawlerMain();
        String string = jm.get(url, param);
        System.out.println(string);
 
        String url2 = "http://www.epeople.go.kr/jsp/user/pc/policy/KsUPcUnionPolicyList.jsp";
        Map<String, String> param2 = new HashMap<String, String>();
        param2.put("no", "1");
        String string2 = jm.get(url2, param2);
        System.out.println(string2);
 
        try {
            client.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
 
    /**
     * POST
     * 
     * @param url       요청할 url
     * @param params    파라미터 Map
     * @param encoding  파라미터 Encoding Type
     * @return 응답본문 문자열
     */
    public String post(String url, Map<String, String> params, String encoding) {
 
        try {
            HttpPost post = new HttpPost(url);
            System.out.println("================================");
            System.out.println("POST : " + post.getURI());
            System.out.println("================================");
 
            List<NameValuePair> paramList = convertParam(params);
            post.setEntity(new UrlEncodedFormEntity(paramList, encoding));
 
            ResponseHandler<String> rh = new BasicResponseHandler();
 
            String execute = client.execute(post, rh);
 
            return execute;
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            //            client.close();
        }
 
        return "error";
    }
 
    public String post(String url, Map<String, String> params) {
        return post(url, params, "UTF-8");
    }
 
    /**
     * GET
     * 
     * @param url       요청할 url
     * @param params    파라미터 Map
     * @param encoding  파라미터 Encoding Type
     * @return 응답본문 문자열
     */
    public String get(String url, Map<String, String> params, String encoding) {
 
        try {
            List<NameValuePair> paramList = convertParam(params);
            HttpGet get = new HttpGet(url + "?" + URLEncodedUtils.format(paramList, encoding));
            System.out.println("================================");
            System.out.println("GET : " + get.getURI());
            System.out.println("================================");
            ResponseHandler<String> rh = new BasicResponseHandler();
 
            String execute = client.execute(get, rh);
 
            return execute;
 
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            //            client.close();
        }
 
        return "error";
    }
 
    public String get(String url, Map<String, String> params) {
        return get(url, params, "UTF-8");
    }
 
    private List<NameValuePair> convertParam(Map<String, String> params) {
        List<NameValuePair> paramList = new ArrayList<NameValuePair>();
        Iterator<String> keys = params.keySet().iterator();
        while (keys.hasNext()) {
            String key = keys.next();
            paramList.add(new BasicNameValuePair(key, params.get(key).toString()));
        }
 
        return paramList;
    }




}

package cbr.crawler;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import javax.swing.text.html.HTML.Tag;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.message.BasicNameValuePair;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

class Main {
	public static void main(String... args) throws FileNotFoundException {
		PrintStream out = System.out;
		System.setOut(new PrintStream("text.txt"));
		long start = System.currentTimeMillis();
		for (int i = 1; i <= 5; ++i) {
			for (ArticleInfo info: getArticleList(i)) {
				Article article = getArticle(info);
				if (article != null) {
					// 여기서 article을 디비에 옇든 파일에 쓰든 꼴리는대로
					System.out.println(article);
					System.out.println("");
				}
			}
		}
		System.setOut(out);
		System.out.println((System.currentTimeMillis() - start) + "ms");
	}

	private static List<ArticleInfo> getArticleList(int pageNum) {
		final String articleListUrl = "http://www.epeople.go.kr/jsp/user/pc/policy/KsUPcUnionPolicyList.jsp";
		System.out.println("getArticleList: " + pageNum);
		Map<String, String> param = new HashMap<String, String>();
		param.put("pageNum", String.valueOf(pageNum));
        param.put("flag", "3");
        param.put("faq_no_n", "");
        param.put("civil_no_c", "");
        param.put("peti_no_c", "");
        param.put("strFrom", "");
        param.put("strTo", "");
        param.put("show_sele", "");
        param.put("sdetail", "1");

        List<ArticleInfo> articles = new ArrayList<>();
		try {
			Document doc = parse(articleListUrl, param);
			if (doc != null) {
				for (Element tal: doc.getElementsByClass("taL")){
					String href = tal.getElementsByTag(Tag.A.toString()).first().attr("href");
					String[] values = href.split("\\(")[1].split("\\)")[0].split(",");
//					String flag = values[0].replaceAll("'", "").trim();
					String faqNo = values[1].replaceAll("'", "").trim();
					String civilNo = values[2].replaceAll("'", "").trim();
					String petiNo = values[3].replaceAll("'", "").trim();
					articles.add(new ArticleInfo(faqNo, civilNo, petiNo));
				}
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
        return articles;
	}

	private static Article getArticle(ArticleInfo info) {
		final String articleUrl = "http://www.epeople.go.kr/jsp/user/pc/policy/UPcUnionPolicyDetail.jsp";
		System.out.println("getArticle: " + info);
		Map<String, String> param = new HashMap<String, String>();
		param.put("sdetail", "1");
		param.put("strFlag", "");
		param.put("strArea", "");
		param.put("strBody", "");
		param.put("cat_name", "");
		param.put("strFrom_ex", "");
		param.put("strTo_ex", "");
		param.put("pageNum", "");
        param.put("flag", "3");
        param.put("faq_no_n", info.faqNo);
        param.put("civil_no_c", info.civilNo);
        param.put("peti_no_c", info.petiNo);
        param.put("strFrom", "");
        param.put("strTo", "");

		try {
			Document doc = parse(articleUrl, param);
			if (doc != null) {
				String question =
					doc.getElementsByClass("questionDiv").first()
						.getElementsByTag(Tag.TABLE.toString()).first()
						.getElementsByTag("tbody").first()
						.getElementsByTag(Tag.TR.toString()).first()
						.getElementsByTag(Tag.TD.toString()).first()
						.getElementsByTag(Tag.DIV.toString()).first()
						.text()
						.replaceAll("<p>", "")
						.replaceAll("<P>", "")
						.replaceAll("</p>", "\n")
						.replaceAll("</P>", "\n");
				
				String answer =
					doc.getElementsByClass("answerTxt").first().text()
						.replaceAll("<p>", "")
						.replaceAll("<P>", "")
						.replaceAll("</p>", "\n")
						.replaceAll("</P>", "\n");
				System.out.println(answer);
				
				Element civilPart = doc.getElementsByClass("civilPart").first();
				String department = civilPart.getElementsByClass("pl10").first().text();
				String law = civilPart.getElementsByClass("pl10").get(1).text();
				
				return new Article(question, answer, department, law);
			}
		} catch (Exception e) {
			/* Nothing to do */
		}
		return null;
	}
	
	private static Document parse(String url, Map<String, String> param) throws ClientProtocolException, IOException {
		List<NameValuePair> paramList = param.entrySet().stream().map(e -> new BasicNameValuePair(e.getKey(), e.getValue().toString())).collect(Collectors.toList());

        HttpClient client = HttpClientBuilder.create().build();
		HttpPost post = new HttpPost(url);
        post.setEntity(new UrlEncodedFormEntity(paramList, StandardCharsets.UTF_8));

//		System.out.println("  # 0: " + post.getURI());
		HttpResponse response = client.execute(post);
//		System.out.println("  # 1");
		HttpEntity entity = response.getEntity();
		int statusCode = response.getStatusLine().getStatusCode();
//		System.out.println("  # 2: " + statusCode);
		BufferedReader br = new BufferedReader(new InputStreamReader(entity.getContent(), Charset.forName("EUC_KR")));
		String html = "", line = null;
		while((line = br.readLine()) != null)
				html += line;
			
//		System.out.println("  # 3");
		if (statusCode == 200) {
			return Jsoup.parse(html.replaceAll("&nbsp;", " "));
		} else {
			System.err.println("statusCode: " + statusCode);
			System.err.println(html);
			return null;
		}
	}

	private static class ArticleInfo {
		public String faqNo;
		public String civilNo;
		public String petiNo;
		ArticleInfo(String faqNo, String civilNo, String petiNo) {
			this.faqNo = faqNo;
			this.civilNo = civilNo;
			this.petiNo = petiNo;
		}
		@Override
		public String toString() {
			return String.join(", ", faqNo, civilNo, petiNo);
		}
	}
	
	private static class Article {
		public String question;
		public String answer;
		public String department;
		public String law;
		Article(String question, String answer, String department, String law) {
			this.question = question;
			this.answer = answer;
			this.department = department;
			this.law = law;
		}
		@Override
		public String toString() {
			return String.join("\n", question, answer, department, law);
		}
	}
}
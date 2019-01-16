package com.stk123.task.sub;

import java.sql.Connection;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.htmlparser.Node;

import com.stk123.bo.Stk;
import com.stk123.bo.StkDictionary;
import com.stk123.bo.StkText;
import com.stk123.task.XueqiuUtils;
import com.stk123.tool.db.TableTools;
import com.stk123.tool.db.util.DBUtil;
import com.stk123.tool.util.ConfigUtils;
import com.stk123.tool.util.EmailUtils;
import com.stk123.tool.util.HtmlUtils;
import com.stk123.tool.util.HttpUtils;
import com.stk123.tool.util.JdbcUtils;
import com.stk123.tool.util.JsonUtils;
import com.stk123.web.StkDict;

/**
 * ѩ���������
 *
 */
public class XueqiuComment {

	public static void main(String[] args) throws Exception {
		System.out.println(XueqiuComment.class.getName());
		ConfigUtils.setPropsFromResource(TableTools.class,"db.properties");
		Connection conn = null;
		List<String> results = new ArrayList<String>();
		try{
			try{
				conn = DBUtil.getConnection();
				Map<String, StkDictionary> comments = StkDict.getDict(StkDict.XUEQIU_COMMENT);
				for(Map.Entry<String, StkDictionary> dict : comments.entrySet()){
					String page = HttpUtils.get("http://xueqiu.com/stock/portfolio/stocks.json?size=1000&pid=-1&tuid="+dict.getKey()+"",null,XueqiuUtils.getCookies(), "gb2312");
					//System.out.println(page);
					Map<String, Class> m = new HashMap<String, Class>();
			        m.put("stocks", Map.class);
					Map map = (Map)JsonUtils.getObject4Json(page, Map.class, m);
					//System.out.println(map.get("stocks"));
					Iterator it = ((List)map.get("stocks")).iterator();
					while(it.hasNext()){
						Map mp = (Map)it.next();
						String comment = String.valueOf(mp.get("comment"));
						if(!"".equals(comment) && !"null".equals(comment)){
							//System.out.println(mp);
							String code = String.valueOf(mp.get("code"));
							code = StringUtils.replace(StringUtils.replace(code, "SH", ""),"SZ","");
							List params = new ArrayList();
							params.add(code);
							Stk stk = JdbcUtils.load(conn, "select code,name from stk where code=?", params, Stk.class);
							if(stk == null)continue;
							params.clear();
							params.add(code);
							String title = "ѩ��[<a target='_blank' href='http://xueqiu.com/"+dict.getKey()+"'>"+dict.getValue().getText()+"</a>]";
							params.add(title);
							//StkText st = JdbcUtils.load(conn, "select * from (select * from stk_text where type=2 and code=? and title=? order by insert_time desc) a limit 0,1", params, StkText.class);
							StkText st = JdbcUtils.load(conn, "select * from (select * from stk_text where type=2 and code=? and title=? order by insert_time desc) where rownum <= 1", params, StkText.class);
							if(st == null || !st.getText().equals(comment)){
								params.clear();
								params.add(code);
								params.add(title);
								params.add(JdbcUtils.createClob(comment));
								//JdbcUtils.insert(conn, "insert into stk_text(id,type,code, code_type,title, text,insert_time,update_time) select s_text_id.nextval,2,?,1,?,?,sysdate(),null from dual", params);
								JdbcUtils.insert(conn, "insert into stk_text(id,type,code, code_type,title, text,insert_time,update_time) select s_text_id.nextval,2,?,1,?,?,sysdate,null from dual", params);
								results.add(title+"--"+stk.getName()+"["+code+"]"+comment);
							}
						}
					}
				}
			}catch(Exception e){
				e.printStackTrace();
			}
			
			//==============================
			
			try{
				Map<String, StkDictionary> articles = StkDict.getDict(StkDict.XUEQIU_ARTICLE);
				for(Map.Entry<String, StkDictionary> dict : articles.entrySet()){
					String page = HttpUtils.get("http://xueqiu.com/statuses/user_timeline.json?user_id="+dict.getKey()+"&page=1&type=0",null, XueqiuUtils.getCookies(), "gb2312");
					//System.out.println(page);
					Map<String, Class> m = new HashMap<String, Class>();
			        m.put("statuses", Map.class);
					Map map = (Map)JsonUtils.getObject4Json(page, Map.class, m);
					//System.out.println(map.get("stocks"));
					Iterator it = ((List)map.get("statuses")).iterator();
					while(it.hasNext()){
						Map mp = (Map)it.next();
						String comment = String.valueOf(mp.get("text"));
						if(!"".equals(comment) && !"null".equals(comment)){
							//System.out.println(comment);
							if(!comment.startsWith("<a"))continue;
							List<Node> links = HtmlUtils.getNodeListByTagName(comment, null, "a");
							if(links != null && links.size() > 0){
								Node a = links.get(0);
								if(StringUtils.indexOf(a.toHtml(), "xueqiu.com/S/") > 0){
									List params = new ArrayList();
									String code = StringUtils.replace(StringUtils.replace(StringUtils.substringBetween(a.toHtml(), "/S/", "\""), "SH", ""),"SZ","");
									code = StringUtils.replace(StringUtils.replace(code, "SH", ""),"SZ","");
									params.add(code);
									Stk stk = JdbcUtils.load(conn, "select code,name from stk where code=?", params, Stk.class);
									if(stk == null)continue;
									
									String title = "ѩ��-[<a target='_blank' href='http://xueqiu.com/"+dict.getKey()+"'>"+dict.getValue().getText()+"</a>]";
									params.clear();
									params.add(code);
									params.add(title);
									StkText st = JdbcUtils.load(conn, "select * from (select * from stk_text where type=2 and code=? and title=? order by insert_time desc) where rownum <= 1", params, StkText.class);
									if(st == null || !st.getText().equals(comment)){
										params.clear();
										params.add(code);
										params.add(title);
										params.add(JdbcUtils.createClob(comment));
										JdbcUtils.insert(conn, "insert into stk_text(id,type,code, code_type,title, text,insert_time,update_time)"
												+ " select s_text_id.nextval,2,?,1,?,?,sysdate,null from dual", params);
										results.add(title+"--"+stk.getName()+"["+code+"]"+comment);
									}
								}
							}
						}
					}
				}
				
			}catch(Exception e){
				e.printStackTrace();
			}
			
			/*if(results.size() > 0){
				EmailUtils.send("ѩ�����˸�������", StringUtils.join(results, "<br>"));
			}*/
		} finally {
			if (conn != null) conn.close();
		}
	}

}

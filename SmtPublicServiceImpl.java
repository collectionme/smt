
package com.smt.service.impl.api;
import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.Serializable;
import java.io.UnsupportedEncodingException;
import java.math.BigDecimal;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.servlet.http.Cookie;

import jodd.util.StringUtil;
import net.sf.json.JsonConfig;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.protocol.HttpClientContext;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.params.CoreConnectionPNames;
import org.apache.http.util.EntityUtils;
import org.apache.log4j.Logger;
import org.jeecgframework.core.common.service.impl.CommonServiceImpl;
import org.jeecgframework.core.util.DateUtils;
import org.jeecgframework.core.util.MyBeanUtils;
import org.jeecgframework.core.util.ResourceUtil;
import org.jeecgframework.core.util.UUIDGenerator;
import org.jeecgframework.p3.core.common.utils.DateUtil;
import org.jeecgframework.p3.core.utils.common.StringUtils;
import org.jeecgframework.web.system.service.SystemService;
import org.json.JSONArray;
import org.json.JSONObject;
import org.jsoup.Connection;
import org.jsoup.Connection.Response;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.alibaba.fastjson.JSON;
import com.fr.third.JAI.SegmentedSeekableStream;
import com.smt.constants.Constants_smt;
import com.smt.controller.spider.HttpUtils;
import com.smt.entity.business.SmtBusinessEntity;
import com.smt.entity.buyer.SmtBuyerEntity;
import com.smt.entity.company.SmtCompanyEntity;
import com.smt.entity.contract.SmtAgentContractEntity;
import com.smt.entity.cus.SmtCertEntity;
import com.smt.entity.cus.SmtDecrationCusEntity;
import com.smt.entity.decl.SmtDecrationCiqEntity;
import com.smt.entity.declar.SmtDeclarComEntity;
import com.smt.entity.financial.SmtFinancialDetailEntity;
import com.smt.entity.freetax.SmtFreetaxDetailEntity;
import com.smt.entity.freetax.SmtFreetaxEntity;
import com.smt.entity.goods.SmtGoodsEntity;
import com.smt.entity.group.SmtGroupDetailEntity;
import com.smt.entity.group.SmtGroupEntity;
import com.smt.entity.history.SmtReturnHistoryEntity;
import com.smt.entity.main.SmtMainInfoEntity;
import com.smt.entity.order.SmtOrderDetailEntity;
import com.smt.entity.order.SmtOrderEntity;
import com.smt.entity.producer.SmtProducerEntity;
import com.smt.entity.prove.SmtProveDetailEntity;
import com.smt.entity.prove.SmtProveEntity;
import com.smt.entity.quality.SmtQualityComEntity;
import com.smt.entity.semen.SmtEncasemenEntity;
import com.smt.entity.semen.SmtEncasementDetailEntity;
import com.smt.entity.statistic.SmtStaticUserEntity;
import com.smt.entity.statistic.SmtStatisticEntity;
import com.smt.entity.user.SmtMarketUserEntity;
import com.smt.service.api.SmtPublicServiceI;
import com.smt.service.api.SubjectIntFaceFacade;
import com.smt.service.decl.SmtDecrationCiqServiceI;
import com.smt.service.freetax.SmtFreetaxServiceI;
import com.smt.service.group.SmtGroupServiceI;
import com.smt.service.order.SmtOrderServiceI;
import com.smt.service.prove.SmtProveServiceI;
import com.smt.service.quality.SmtQualityComServiceI;
import com.smt.service.semen.SmtEncasemenServiceI;
import com.smt.utils.CryptAES;
import com.smt.utils.DesEcrypt;
import com.smt.utils.HttpUtil;
import com.smt.utils.JsonDateValueProcessor;
import com.smt.utils.ProgressSingleton;
import com.smt.utils.QueryUtil;
import com.smt.utils.SmsUtil;
import com.smt.utils.StringToXML;

import ij.gui.ProgressBar;

@Service("smtPublicService")
@Transactional
public class SmtPublicServiceImpl extends CommonServiceImpl implements SmtPublicServiceI {
	
	private static final Logger logger = Logger.getLogger(SmtPublicServiceI.class);
	private static DesEcrypt asd =new DesEcrypt();//加密
	
	@Autowired
	private SmtOrderServiceI smtOrderService;
	@Autowired
	private SmtGroupServiceI smtGroupService;
	@Autowired
	private SmtEncasemenServiceI smtEncasemenService;
	@Autowired
	private SubjectIntFaceFacade subjectIntFaceFacade;
	@Autowired
	private SmtProveServiceI smtProveService;
	@Autowired
	private SmtDecrationCiqServiceI smtDecrationCiqService;
	@Autowired
	private SmtFreetaxServiceI smtFreetaxService;
	@Autowired
	private SystemService systemService;
	/**
	 * 商户备案
	 * @throws Exception 
	 */
	public String execComp(String opType, Map<String,Object> company) throws Exception{
		String compXmlStr = this.getCompXmlStr(opType, company);
		logger.info(DateUtils.getDate("yyyy-MM-dd HH:mm:ss")+" 商户备案Request："+compXmlStr);
		//字符串转文件
		String reqFile = DateUtils.getDate("yyyyMMddHHmmss")+".xml";
		StringToXML.string2File(compXmlStr, ResourceUtil.getConfigByName("xmlpath")+"1-comp/"+reqFile);
		String response = this.subjectIntFaceFacade.sendDeclaration(ResourceUtil.getConfigByName("corpCode"),ResourceUtil.getConfigByName("corpName"),ResourceUtil.getConfigByName("loginCode"),ResourceUtil.getConfigByName("loginPassWord"), compXmlStr);
		logger.info(DateUtils.getDate("yyyy-MM-dd HH:mm:ss")+" 商户备案Respons："+response);
		if(StringUtils.isEmpty(response)){
			logger.info(DateUtils.getDate("yyyy-MM-dd HH:mm:ss")+" 商户备案_接口回执为空");
		    return "{\"result\":\"0\",\"errorMessage\":\"市场采购贸易系统未返回回执！\"}";
		}
		JSONObject jsonObject =new JSONObject(response);
		String returnStatus = "提交失败";
		if((Integer)jsonObject.get("result")==1 && !jsonObject.isNull("otherMessage")){
			returnStatus = "提交成功";
			JSONArray jsonArray = jsonObject.getJSONArray("otherMessage");
			JSONObject map  = (JSONObject) jsonArray.get(0);
		    String sql = "update smt_company set status = ?, status_desc = ?, org_id = ?, create_date = ? where id = ?";
			this.executeSql(sql, new Object[]{Constants_smt.SMT_COMPANY_STATUS_YTJ, "核实中(管委会)",map.get("orgId"),DateUtils.getDate("yyyy-MM-dd HH:mm:ss"),company.get("id")});
			company.put("status", Constants_smt.SMT_COMPANY_STATUS_YTJ);
			//短信提醒  0:不发送，1:发送(默认)
			if(Integer.parseInt(ResourceUtil.getConfigByName("sms.tip.control"))==1){
				String content = "尊敬的"+company.get("create_name")+",您在圣贸通外贸综合服务平台成功提交1039商户备案资料，核实通过后将短信通知。";
				SmsUtil.sendSms(content, company.get("phone").toString());
				//SmsUtil.sendSms(content, "13416406116");
			}
		}else if(response.contains("统一社会信用代码已存在")){
			returnStatus = "该企业已在其他平台备案";
			String sql = "update smt_company set status = ?, status_desc = ? where id = ?";
			this.executeSql(sql, new Object[]{Constants_smt.SMT_COMPANY_STATUS_HSTG, "核实通过",company.get("id")});
			company.put("status", Constants_smt.SMT_COMPANY_STATUS_HSTG);
		}
		//回执记录
		SmtReturnHistoryEntity t = new SmtReturnHistoryEntity();
		t.setReturnStatus(returnStatus);
		t.setReturnMessage(response);
		t.setReturnTime(DateUtil.str2Date(DateUtils.date2Str(new Date(), DateUtils.datetimeFormat)));
		if(company.get("id")!=null||!"".equals(company.get("id").toString())){
			t.setYwid(company.get("id").toString());
			this.save(t);
		}
		return response;
	}
	
	/**
	 * 企业备案
	 * @throws Exception 
	 */
	public String execBusiness(String opType, Map<String,Object> business) throws Exception{
		String businessXmlStr = this.getBusinessXmlStr(opType, business);
		logger.info(DateUtils.getDate("yyyy-MM-dd HH:mm:ss")+" 企业备案Request："+businessXmlStr);
		//字符串转文件
		String fileName = DateUtils.getDate("yyyyMMddHHmmss")+".xml";
		StringToXML.string2File(businessXmlStr, ResourceUtil.getConfigByName("xmlpath")+"1-comp/"+fileName);
		String response = this.subjectIntFaceFacade.sendDeclaration(ResourceUtil.getConfigByName("corpCode"),ResourceUtil.getConfigByName("corpName"),ResourceUtil.getConfigByName("loginCode"),ResourceUtil.getConfigByName("loginPassWord"), businessXmlStr);
		logger.info(DateUtils.getDate("yyyy-MM-dd HH:mm:ss")+" 企业备案Response："+response);
		if(StringUtils.isEmpty(response)){
			logger.info(DateUtils.getDate("yyyy-MM-dd HH:mm:ss")+" 企业备案_接口回执为空");
		    return "{\"result\":\"0\",\"errorMessage\":\"市场采购贸易系统未返回回执！\"}";
		}
		JSONObject jsonObject =new JSONObject(response);
		String returnStatus = "提交失败";
		if((Integer)jsonObject.get("result")==1 && !jsonObject.isNull("otherMessage")){
			returnStatus = "提交成功";
			JSONArray jsonArray = jsonObject.getJSONArray("otherMessage");
			JSONObject map  = (JSONObject) jsonArray.get(0);
			String sql = "update smt_business set status = ?, status_desc = ?, org_id = ?, create_date = ? where id = ?";
			this.executeSql(sql, new Object[]{Constants_smt.SMT_COMPANY_STATUS_YTJ, "核实中(管委会)",map.get("orgId"),DateUtils.getDate("yyyy-MM-dd HH:mm:ss"),business.get("id")});
			business.put("status", Constants_smt.SMT_COMPANY_STATUS_YTJ);
			//短信提醒  0:不发送，1:发送(默认)
			if(Integer.parseInt(ResourceUtil.getConfigByName("sms.tip.control"))==1){
				String content = "尊敬的"+business.get("create_name")+",您在圣贸通外贸综合服务平台成功提交1039商户备案资料，核实通过后将短信通知。";
				SmsUtil.sendSms(content, business.get("phone").toString());
				//SmsUtil.sendSms(content, "13416406116");
			}
		}else if(response.contains("统一社会信用代码已存在")){
			returnStatus = "该企业已在其他平台备案";
			String sql = "update smt_business set status = ?, status_desc = ? where id = ?";
			this.executeSql(sql, new Object[]{Constants_smt.SMT_COMPANY_STATUS_HSTG, "核实通过", business.get("id")});
			business.put("status", Constants_smt.SMT_COMPANY_STATUS_HSTG);
		}
		//回执记录
		SmtReturnHistoryEntity t = new SmtReturnHistoryEntity();
		t.setReturnStatus(returnStatus);
		t.setReturnMessage(response);
		t.setReturnTime(DateUtil.str2Date(DateUtils.date2Str(new Date(), DateUtils.datetimeFormat)));
		if(business.get("id")!=null||!"".equals(business.get("id").toString())){
			t.setYwid(business.get("id").toString());
			this.save(t);
		}
		return response;
	}
	
	/**
	 * 采购商备案
	 * @param opType
	 * @param sender
	 * @param data
	 * @throws Exception 
	 */
	public String execBuyer(String opType, Map<String, Object> map) throws Exception{
		String status = "1";//审核通过
		String returnStatus = "提交成功";
		String buyerXmlStr = this.getBuyerXmlStr(opType, map);
		logger.info(DateUtils.getDate("yyyy-MM-dd HH:mm:ss")+" 采购商备案Request："+buyerXmlStr);
		//字符串转文件
		String fileName = map.get("create_name")+DateUtils.getDate("yyyyMMddHHmmss")+".log";
		StringToXML.string2File(buyerXmlStr, ResourceUtil.getConfigByName("xmlpath")+"2-buyer/"+fileName);
		String response = this.subjectIntFaceFacade.sendDeclaration(ResourceUtil.getConfigByName("corpCode"),ResourceUtil.getConfigByName("corpName"),ResourceUtil.getConfigByName("loginCode"),ResourceUtil.getConfigByName("loginPassWord"), buyerXmlStr);
		logger.info(DateUtils.getDate("yyyy-MM-dd HH:mm:ss")+" 采购商备案Response："+response);
		if(StringUtils.isEmpty(response)){
			logger.info(DateUtils.getDate("yyyy-MM-dd HH:mm:ss")+" 采购商备案_接口回执为空");
		    return "{\"result\":\"0\",\"errorMessage\":\"市场采购贸易系统未返回回执！\"}";
		}
		JSONObject jsonObject =new JSONObject(response);
		if(!jsonObject.isNull("errorMessage")){
			if(!response.contains("私有数据已存在不需要新建")){
				status = "0";
				returnStatus = "提交失败";
			}
		}
		String sql = "update smt_buyer set status = ? where id = ?";
		this.executeSql(sql, new Object[]{status, map.get("id")});
		map.put("status", status);
		//回执记录
		SmtReturnHistoryEntity t = new SmtReturnHistoryEntity();
		t.setReturnStatus(returnStatus);
		t.setReturnMessage(response);
		t.setReturnTime(DateUtil.str2Date(DateUtils.date2Str(new Date(), DateUtils.datetimeFormat)));
		t.setYwid(map.get("id").toString());
		this.save(t);
		return response;
	}
	
	/**
	 * 生产商备案
	 * @param opType
	 * @param data
	 */
	public String execProducer(String opType, Map<String, Object> map) throws Exception{
		String status = "1";
		String returnStatus = "提交成功";
		String mafXmlStr = this.getMafXmlStr(opType, map);
		logger.info(DateUtils.getDate("yyyy-MM-dd HH:mm:ss")+" 生产商备案Request："+mafXmlStr);
		//字符串转文件
		String fileName = map.get("create_name")+DateUtils.getDate("yyyyMMddHHmmss")+".log";
		StringToXML.string2File(mafXmlStr, ResourceUtil.getConfigByName("xmlpath")+"3-maf/"+fileName);		
		String response = this.subjectIntFaceFacade.sendDeclaration(ResourceUtil.getConfigByName("corpCode"),ResourceUtil.getConfigByName("corpName"),ResourceUtil.getConfigByName("loginCode"),ResourceUtil.getConfigByName("loginPassWord"), mafXmlStr);
		logger.info(DateUtils.getDate("yyyy-MM-dd HH:mm:ss")+" 生产商备案Response："+response);
		if(StringUtils.isEmpty(response)){
			logger.info(DateUtils.getDate("yyyy-MM-dd HH:mm:ss")+" 生产商备案_接口回执为空");
		    return "{\"result\":\"0\",\"errorMessage\":\"市场采购贸易系统未返回回执！\"}";
		}
		JSONObject jsonObject =new JSONObject(response);
		if(!jsonObject.isNull("errorMessage")){
			if(!response.contains("私有数据已存在不需要新建")){
				status = "0";
				returnStatus = "提交失败";
			}
		}
		String sql = "update smt_producer set status = ? where id = ?";
		this.executeSql(sql, new Object[]{status,map.get("id")});
		map.put("status", status);
		//回执记录
		SmtReturnHistoryEntity t = new SmtReturnHistoryEntity();
		t.setReturnStatus(returnStatus);
		t.setReturnMessage(response);
		t.setReturnTime(DateUtil.str2Date(DateUtils.date2Str(new Date(), DateUtils.datetimeFormat)));
		t.setYwid(map.get("id").toString());
		this.save(t);
		return response;
	}
	
	/**
	 * 商品备案
	 * @param opType
	 * @param data
	 * @return
	 */
	public String execGoods(String opType,  Map<String, Object> data) throws Exception{
		String goodsXmlStr = this.getGoodsXmlStr(opType, data);
		logger.info(DateUtils.getDate("yyyy-MM-dd HH:mm:ss")+" 商品备案Request："+goodsXmlStr);
		//字符串转文件
		String fileName = data.get("create_name")+DateUtils.getDate("yyyyMMddHHmmss")+".log";
		StringToXML.string2File(goodsXmlStr, ResourceUtil.getConfigByName("xmlpath")+"4-goods/"+fileName);		
		String response = this.subjectIntFaceFacade.sendDeclaration(ResourceUtil.getConfigByName("corpCode"),ResourceUtil.getConfigByName("corpName"),ResourceUtil.getConfigByName("loginCode"),ResourceUtil.getConfigByName("loginPassWord"), goodsXmlStr);
		logger.info(DateUtils.getDate("yyyy-MM-dd HH:mm:ss")+" 商品备案Response："+response);
		if(StringUtils.isEmpty(response)){
			logger.info(DateUtils.getDate("yyyy-MM-dd HH:mm:ss")+" 商品备案_接口回执为空");
		    return "{\"result\":\"0\",\"errorMessage\":\"市场采购贸易系统未返回回执！\"}";
		}
		JSONObject jsonObject =new JSONObject(response);
		String returnStatus = "提交失败";
		if(!jsonObject.isNull("otherMessage")){
			returnStatus = "提交成功";
			JSONArray jsonArray = jsonObject.getJSONArray("otherMessage");
			JSONObject map  = (JSONObject) jsonArray.get(0);
		    String sql = "update smt_goods set status =	?,  cg_goods_code = ? where id = ?";
			this.executeSql(sql, new Object[]{Constants_smt.SMT_GOODS_STATUS_YTJ,map.get("goodsCode"),map.get("goodsId")});
			data.put("status", Constants_smt.SMT_GOODS_STATUS_YTJ);
			data.put("goods_code", map.get("goodsCode"));
		}
		//回执记录
		SmtReturnHistoryEntity t = new SmtReturnHistoryEntity();
		t.setReturnStatus(returnStatus);
		t.setReturnMessage(response);
		t.setReturnTime(DateUtil.str2Date(DateUtils.date2Str(new Date(), DateUtils.datetimeFormat)));
		t.setYwid(data.get("id").toString());
		this.save(t);		
		return response;
	}
	
	/**
	 * 订单备案
	 * @param opType
	 * @param data
	 * @return
	 */
	public Map<String ,String> execOrder(String opType,  Map<String, Object> data) throws Exception{
		String tradeXmlStr = this.getTradeXmlStr(opType, data);
		logger.info(DateUtils.getDate("yyyy-MM-dd HH:mm:ss")+" 订单提交Request："+tradeXmlStr);
		//字符串转文件
		String fileName = data.get("create_name")+DateUtils.getDate("yyyyMMddHHmmss")+".log";
		StringToXML.string2File(tradeXmlStr, ResourceUtil.getConfigByName("xmlpath")+"5-trad/"+fileName);		
		String response = this.subjectIntFaceFacade.sendDeclaration(ResourceUtil.getConfigByName("corpCode"),ResourceUtil.getConfigByName("corpName"),ResourceUtil.getConfigByName("loginCode"),ResourceUtil.getConfigByName("loginPassWord"), tradeXmlStr);
		logger.info(DateUtils.getDate("yyyy-MM-dd HH:mm:ss")+" 订单提交Response："+response);
		//mapDarte用于装返回数据
		Map <String ,String> mapDate=new HashMap<String ,String>();
		String stature="";
		String note="";
		if(StringUtils.isEmpty(response)){
			logger.info(DateUtils.getDate("yyyy-MM-dd HH:mm:ss")+" 交易单录入_接口回执为空");
			Map <String ,String> map=new HashMap<String ,String>();
			map.put("response", "{\"result\":\"0\",\"errorMessage\":\"市场采购贸易系统未返回回执！\"}");
			map.put("note", "");
			map.put("status", "1");
		    return map;
		}
		JSONObject jsonObject =new JSONObject(response);
		String returnStatus = "提交失败";
		if((Integer)jsonObject.get("result")==1 && !jsonObject.isNull("otherMessage")){
			JSONArray jsonArray = jsonObject.getJSONArray("otherMessage");
			JSONObject otherMessage  = (JSONObject) jsonArray.get(0);
			String exceptionCode0=otherMessage.get("exceptionCode0").toString();
			
			//提交正常或者有异常但是填写了备注
			if(exceptionCode0.equals("0")){
				returnStatus = "提交成功";
				stature="0";
				//更新交易单
				String sql0 = "update smt_order set order_sn =?,status = ? where id = ?";
				this.executeSql(sql0, new Object[]{otherMessage.get("tradingNo0"), Constants_smt.SMT_ORDER_STATUS_YTJ, data.get("id")});
				data.put("status", Constants_smt.SMT_ORDER_STATUS_YTJ);
				data.put("order_sn", otherMessage.get("tradingNo0"));
				
				//更新交易单明细
				JSONObject tradeDetail0 = (JSONObject) otherMessage.get("tradeDetail0");
				if(tradeDetail0.length()>0){
					for (int i = 0; i < tradeDetail0.length()/2; i++) {
						String sql = "update smt_order_detail set trade_detail_id_return = ? where id = ?";
						this.executeSql(sql, new Object[]{tradeDetail0.get("tradeDetailIdReturn"+i), tradeDetail0.get("tradeDetailId"+i)});
					}
				}
				//订单状态记录
			 	String sql2 = "update smt_order_state set order_state = ?,order_time = ? where order_id = ?";
			 	this.executeSql(sql2, new Object[]{Constants_smt.SMT_ORDER_STATUS_YTJ, DateUtils.formatDate(new Date(), "yyyy-MM-dd HH:mm:ss"), data.get("id")});
			}
			//提交有异常，系统未提交交易单
			else{
				returnStatus = "提交失败";
				stature="1";
				//添加
				String solveWay0=otherMessage.getString("solveWay0").toString();
				String exceptionMsg0=otherMessage.getString("exceptionMsg0");
				 note=","+solveWay0+","+exceptionMsg0;
				//更新交易单
				String sql0 = "update smt_order set order_sn =?,status = ?,note=? where id = ?";
				this.executeSql(sql0, new Object[]{otherMessage.get("tradingNo0"), Constants_smt.SMT_ORDER_STATUS_YLR,note, data.get("id")});
				data.put("status", Constants_smt.SMT_ORDER_STATUS_YLR);
				data.put("order_sn", otherMessage.get("tradingNo0"));
				//更新交易单明细
				JSONObject tradeDetail0 = (JSONObject) otherMessage.get("tradeDetail0");
				if(tradeDetail0.length()>0){
					for (int i = 0; i < tradeDetail0.length()/2; i++) {
						String sql = "update smt_order_detail set trade_detail_id_return = ? where id = ?";
						this.executeSql(sql, new Object[]{tradeDetail0.get("tradeDetailIdReturn"+i), tradeDetail0.get("tradeDetailId"+i)});
					}
				}
				//订单状态记录
			 	String sql2 = "update smt_order_state set order_state = ?,order_time = ? where order_id = ?";
			 	this.executeSql(sql2, new Object[]{Constants_smt.SMT_ORDER_STATUS_YLR, DateUtils.formatDate(new Date(), "yyyy-MM-dd HH:mm:ss"), data.get("id")});		
			}
		}
		SmtReturnHistoryEntity t = new SmtReturnHistoryEntity();
		t.setReturnStatus(returnStatus);
		t.setReturnMessage(response);
		t.setReturnTime(DateUtil.str2Date(DateUtils.date2Str(new Date(), DateUtils.datetimeFormat)));
		t.setYwid(data.get("id").toString());
		this.save(t);
		
		mapDate.put("response", response);
		mapDate.put("note", note);
		mapDate.put("status", stature);
	    return mapDate;
	}
	
	/**
	 * 委托代理出口协议
	 * @param opType
	 * @param client_code
	 * @return
	 */
	public Map<String,Object> execCont(String opType, SmtMarketUserEntity userEntity) throws Exception{
		String contXmlStr = this.getContXmlStr(opType, userEntity.getRegNum());
		logger.info(DateUtils.getDate("yyyy-MM-dd HH:mm:ss")+" 委托代理出口协议Request："+contXmlStr);
		String response = this.subjectIntFaceFacade.sendDeclaration(ResourceUtil.getConfigByName("corpCode"),ResourceUtil.getConfigByName("corpName"),ResourceUtil.getConfigByName("loginCode"),ResourceUtil.getConfigByName("loginPassWord"), contXmlStr);
		logger.info(DateUtils.getDate("yyyy-MM-dd HH:mm:ss")+" 委托代理出口协议Response："+response);
		Map<String,Object> queryRes=new HashMap<String,Object>();
		if(StringUtils.isNotEmpty(response)){
			JSONObject jsonObject =new JSONObject(response);
			if(Integer.parseInt(jsonObject.get("result").toString())==1){
				if(!jsonObject.isNull("otherMessage")){
					JSONArray jsonArray = jsonObject.getJSONArray("otherMessage");
					for(int a=0;a<jsonArray.length();a++){
						JSONObject json=jsonArray.getJSONObject(a);
						queryRes= this.findContract(userEntity,json.get("contractNo"+a).toString());
					}
				}
			}else{
				queryRes.put("result", jsonObject.get("result").toString());
				if(jsonObject.has("message")){
					queryRes.put("msg", jsonObject.get("message").toString());
				}else{
					queryRes.put("msg", "生成失败！请稍后重试");
				}
			}
			logger.info(DateUtils.getDate("yyyy-MM-dd HH:mm:ss")+"查询委托代理出口协议Response："+queryRes);
		}else{
			throw new Exception("市场采购系统未返回回执报文，请联系管理员！");
		}
		return queryRes;
	}
	
	/**
	 * 组货单备案
	 * @param opType
	 * @param map
	 * @return
	 * @throws Exception 
	 */
	public String execComb(String opType,Map<String, Object> map) throws Exception{
		String combXmlStr = this.getCombXmlStr(opType, map);
		logger.info(DateUtils.getDate("yyyy-MM-dd HH:mm:ss")+" 组货单Request："+combXmlStr);
		//字符串转文件
		String fileName = map.get("create_name")+DateUtils.getDate("yyyyMMddHHmmss")+".log";
		StringToXML.string2File(combXmlStr, ResourceUtil.getConfigByName("xmlpath")+"6-comb/"+fileName);
		String response = this.subjectIntFaceFacade.sendDeclaration(ResourceUtil.getConfigByName("corpCode"),ResourceUtil.getConfigByName("corpName"),ResourceUtil.getConfigByName("loginCode"),ResourceUtil.getConfigByName("loginPassWord"), combXmlStr);
		logger.info(DateUtils.getDate("yyyy-MM-dd HH:mm:ss")+" 组货单Response："+response);
		if(StringUtils.isEmpty(response)){
			logger.info(DateUtils.getDate("yyyy-MM-dd HH:mm:ss")+" 组货单录入_接口回执为空");
		    return "{\"result\":\"0\",\"errorMessage\":\"市场采购贸易系统未返回回执！\"}";
		}
		JSONObject jsonObject =new JSONObject(response);
		//回执记录
		SmtReturnHistoryEntity t = new SmtReturnHistoryEntity();
		t.setReturnStatus(jsonObject.get("result").toString());
		t.setReturnMessage(response);
		t.setReturnTime(DateUtil.str2Date(DateUtils.date2Str(new Date(), DateUtils.datetimeFormat)));
		t.setYwid(map.get("id").toString());
		this.save(t);		
		if(!jsonObject.isNull("otherMessage")){
			JSONArray jsonArray = jsonObject.getJSONArray("otherMessage");
			JSONObject otherMessage  = (JSONObject) jsonArray.get(0);
			String sql0 = "update smt_group set group_sn =?,status = 50002 where id = ?";
			this.executeSql(sql0, new Object[]{otherMessage.get("orderCode"), map.get("id")});
			map.put("status", Constants_smt.SMT_GROUP_STATUS_YTJ);
			map.put("group_sn",otherMessage.get("orderCode"));
			//资金明细
			String hql0 = "from SmtGroupDetailEntity where 1 = 1 AND gROUP_ID = ? ";
		 	List<SmtGroupDetailEntity> smtGroupDetailOldList = this.findHql(hql0,map.get("id"));
		 	for (SmtGroupDetailEntity smtGroupDetailEntity : smtGroupDetailOldList) {
		 		SmtOrderEntity order = this.get(SmtOrderEntity.class, smtGroupDetailEntity.getOrderId());
		 		SmtFinancialDetailEntity financialDetail = new SmtFinancialDetailEntity();
		 		financialDetail.setOrderId(order.getId());
		 		financialDetail.setOrderSn(order.getOrderSn());
		 		financialDetail.setPurpose("代理费用");
		 		financialDetail.setDetail("外贸代理费用");
		 		financialDetail.setAmount(order.getWmPrice());
		 		financialDetail.setPayer(order.getCreateName());
		 		financialDetail.setPayerId(order.getCreateBy());
		 		financialDetail.setPayee(order.getGoodsStore());
		 		financialDetail.setPayeeId(order.getGroupComId());
		 		financialDetail.setStatus("0");
		 		this.save(financialDetail);
		 		//回调商城接口
				/*SmtOrderEntity smtOrderEntity = this.get(SmtOrderEntity.class, smtGroupDetailEntity.getOrderId());
				if(smtOrderEntity!=null && StringUtil.isNotEmpty(smtOrderEntity.getNumber())){
					LinkedHashMap<String, Object> resultMap = new LinkedHashMap<String, Object>();
					resultMap.put("group_status", Constants_smt.SMT_ORDER_STATUS_ZHYTJ);
					resultMap.put("number", smtOrderEntity.getNumber());
					resultMap.put("msg", Constants_smt.getSmtOrderMapLabel(Constants_smt.SMT_ORDER_STATUS_ZHYTJ));
					SoapResponseUtil.responseSoap(JSON.toJSONString(resultMap), "TN0001");
				}*/
		 		String sqlorder = "update smt_order set is_entry ='1' where id = ?";
				this.executeSql(sqlorder, new Object[]{smtGroupDetailEntity.getOrderId()});
		 		//订单表
		 		String sql1 = "update smt_order set status = ? where id = ?";
		 		this.executeSql(sql1, new Object[]{Constants_smt.SMT_ORDER_STATUS_YZH, smtGroupDetailEntity.getOrderId()});
		 		//订单状态表记录
				String sql2 = "update smt_order_state set group_state = ?,group_time = ? where order_id = ?";
				this.executeSql(sql2, new Object[]{Constants_smt.SMT_ORDER_STATUS_YZH, DateUtils.formatDate(new Date(), "yyyy-MM-dd HH:mm:ss"), smtGroupDetailEntity.getOrderId()});
			}
		}
		return response;
	}
	
	/**
	 * 装箱单备案
	 * @param opType
	 * @param packing
	 * @return
	 * @throws Exception 
	 */
	public String execPack(String opType,Map<String, Object> packing) throws Exception{
		String packXmlStr = this.getPackXmlStr(opType, packing);
		logger.info(DateUtils.getDate("yyyy-MM-dd HH:mm:ss")+" 装箱单Request："+packXmlStr);
		//字符串转文件
		String fileName = packing.get("create_name")+DateUtils.getDate("yyyyMMddHHmmss")+".log";
		StringToXML.string2File(packXmlStr, ResourceUtil.getConfigByName("xmlpath")+"7-pack/"+fileName);
		String response = this.subjectIntFaceFacade.sendDeclaration(ResourceUtil.getConfigByName("corpCode"),ResourceUtil.getConfigByName("corpName"),ResourceUtil.getConfigByName("loginCode"),ResourceUtil.getConfigByName("loginPassWord"), packXmlStr);
		logger.info(DateUtils.getDate("yyyy-MM-dd HH:mm:ss")+" 装箱单Response："+response);
		if(StringUtils.isEmpty(response)){
			logger.info(DateUtils.getDate("yyyy-MM-dd HH:mm:ss")+" 装箱单录入_接口回执为空");
		    return "{\"result\":\"0\",\"errorMessage\":\"市场采购贸易系统未返回回执！\"}";
		}		
		JSONObject jsonObject =new JSONObject(response);
		//回执记录
		SmtReturnHistoryEntity t = new SmtReturnHistoryEntity();
		t.setReturnStatus(jsonObject.get("result").toString());
		t.setReturnMessage(response);
		t.setReturnTime(DateUtil.str2Date(DateUtils.date2Str(new Date(), DateUtils.datetimeFormat)));
		t.setYwid(packing.get("id").toString());
		this.save(t);
		if(!jsonObject.isNull("otherMessage")){
			JSONArray jsonArray = jsonObject.getJSONArray("otherMessage");
			JSONObject otherMessage  = (JSONObject) jsonArray.get(0);
			String sql0 = "update smt_encasemen set encase_no =?,status = 60002 where id = ?";
			this.executeSql(sql0, new Object[]{otherMessage.get("billNo"), packing.get("id")});
			packing.put("status", Constants_smt.SMT_ORDER_STATUS_ZXYTJ);
			packing.put("encase_no",otherMessage.get("billNo"));
			//资金明细
		 	String hql0 = "from SmtEncasementDetailEntity where 1 = 1 AND eNCASE_ID = ? ";
			List<SmtEncasementDetailEntity> smtEncasementDetailOldList = this.findHql(hql0,packing.get("id"));
			if(smtEncasementDetailOldList!=null && smtEncasementDetailOldList.size()>0){
				for (SmtEncasementDetailEntity smtEncasementDetailEntity : smtEncasementDetailOldList) {
					//组货表
					String sql1 = "update smt_group set status = ? where id = ?";
					this.executeSql(sql1, new Object[]{Constants_smt.SMT_ORDER_STATUS_ZXYTJ, smtEncasementDetailEntity.getGroupId()});
				 	SmtGroupEntity group = this.get(SmtGroupEntity.class, smtEncasementDetailEntity.getGroupId());
				 	String orderSql = 
				 			"SELECT\n" +
						 	"	smt_order.*\n" +
						 	"FROM\n" +
						 	"	smt_group_detail,\n" +
						 	"	smt_order\n" +
						 	"WHERE\n" +
						 	"	smt_group_detail.order_id = smt_order.id\n" +
						 	"AND smt_group_detail.group_id = ?";
				 	List<Map<String, Object>> orderList = this.findForJdbc(orderSql, new Object[]{smtEncasementDetailEntity.getGroupId()});
				 	if(orderList!=null && orderList.size()>0){
				 		for (Map<String, Object> order : orderList) {
				 			//订单状态表记录
							String sql2 = "update smt_order_state set packing_state = ?,packing_time = ? where order_id = ?";
							this.executeSql(sql2, new Object[]{Constants_smt.SMT_ORDER_STATUS_ZXYTJ, DateUtils.formatDate(new Date(), "yyyy-MM-dd HH:mm:ss"), order.get("id")});
							//订单表
				 			String sql3 = "update smt_order set status = ? where id = ?";
				 			this.executeSql(sql3, new Object[]{Constants_smt.SMT_ORDER_STATUS_ZXYTJ, order.get("id")});
				 			
				 			SmtFinancialDetailEntity financialDetail = new SmtFinancialDetailEntity();
						 	financialDetail.setOrderId(order.get("id").toString());
						 	financialDetail.setOrderSn(order.get("order_sn").toString());
						 	financialDetail.setPurpose("代理费用");
						 	financialDetail.setDetail("货代代理费用");
						 	financialDetail.setAmount(group.getHdPrice());
						 	financialDetail.setPayer(order.get("create_name").toString());
					 		financialDetail.setPayerId(order.get("create_by").toString());
					 		financialDetail.setPayee(group.getDeputyName());
					 		financialDetail.setPayeeId(group.getDeputyId());
					 		financialDetail.setStatus("0");
					 		this.save(financialDetail);
						}
				 	}
				 	
				}
			}
		}
		return response;
	}
	
	/**
	 * 报关单
	 * @param opType
	 * @param custom
	 * @return
	 */
	public String execCustom(String opType,Map<String, Object> custom) throws Exception{
		String customXmlStr = this.getCustomXmlStr(opType, custom);
		logger.info(DateUtils.getDate("yyyy-MM-dd HH:mm:ss")+" 报关单Request："+customXmlStr);
		//字符串转文件
		String fileName = custom.get("create_name")+DateUtils.getDate("yyyyMMddHHmmss")+".log";
		StringToXML.string2File(customXmlStr, ResourceUtil.getConfigByName("xmlpath")+"8-custom/"+fileName);
		String response = this.subjectIntFaceFacade.sendDeclaration(ResourceUtil.getConfigByName("corpCode"),ResourceUtil.getConfigByName("corpName"),ResourceUtil.getConfigByName("loginCode"),ResourceUtil.getConfigByName("loginPassWord"), customXmlStr);
		logger.info(DateUtils.getDate("yyyy-MM-dd HH:mm:ss")+" 报关单response："+response);
		if(StringUtil.isNotEmpty(response)){
			JSONObject jsonObject =new JSONObject(response);
			//回执记录
			SmtReturnHistoryEntity t = new SmtReturnHistoryEntity();
			t.setReturnStatus(jsonObject.get("result").toString());
			t.setReturnMessage(response);
			t.setReturnTime(DateUtil.str2Date(DateUtils.date2Str(new Date(), DateUtils.datetimeFormat)));
			t.setYwid(custom.get("id").toString());
			this.save(t);
			if((Integer)jsonObject.get("result")==1 && !jsonObject.isNull("return")){
				JSONArray jsonArray = jsonObject.getJSONArray("return");
				JSONObject otherMessage  = (JSONObject) jsonArray.get(0);
				if(opType.equals("A")){
					String sql0 = "update smt_decration_cus set number = ?,cop_id = ?,status = 80002 where id = ?";
					this.executeSql(sql0, new Object[]{otherMessage.get("seqNo"), otherMessage.get("copId"), custom.get("id")});
					custom.put("cus_no", otherMessage.get("seqNo"));
				}else{
					String sql0 = "update smt_decration_cus set status = 80002 where id = ?";
					this.executeSql(sql0, new Object[]{custom.get("id")});
					custom.put("cus_no", custom.get("number"));
				}
				custom.put("status", Constants_smt.SMT_ORDER_STATUS_BGYFS);
				
				//资金明细
				String hql0 = "from SmtEncasementDetailEntity where 1 = 1 AND eNCASE_ID = ? ";
				List<SmtEncasementDetailEntity> smtEncasementDetailOldList = this.findHql(hql0, custom.get("encase_id"));
				SmtEncasemenEntity smtEncasemenEntity = this.get(SmtEncasemenEntity.class, custom.get("encase_id").toString());
				if(smtEncasementDetailOldList!=null && smtEncasementDetailOldList.size()>0){
					for (SmtEncasementDetailEntity smtEncasementDetailEntity : smtEncasementDetailOldList) {
						String hql1 = "from SmtGroupDetailEntity where 1 = 1 AND gROUP_ID = ? ";
						List<SmtGroupDetailEntity> smtGroupDetailOldList = this.findHql(hql1,smtEncasementDetailEntity.getGroupId());
						for (SmtGroupDetailEntity smtGroupDetailEntity : smtGroupDetailOldList) {
							//更新交易单状态
							String orderSql = "update smt_order set status = ? where id = ?";
							this.executeSql(orderSql, new Object[]{Constants_smt.SMT_ORDER_STATUS_YBG, smtGroupDetailEntity.getOrderId()});
							
							SmtOrderEntity order = this.get(SmtOrderEntity.class, smtGroupDetailEntity.getOrderId());
						 	SmtFinancialDetailEntity financialDetail = new SmtFinancialDetailEntity();
						 	financialDetail.setOrderId(order.getId());
						 	financialDetail.setOrderSn(order.getOrderSn());
						 	financialDetail.setPurpose("代理费用");
						 	financialDetail.setDetail("报关代理费用");
						 	financialDetail.setAmount(smtEncasemenEntity.getBgPrice());
						 	financialDetail.setPayer(order.getCreateName());
						 	financialDetail.setPayerId(order.getCreateBy());
						 	financialDetail.setPayee(smtEncasemenEntity.getDecCustomName());
						 	financialDetail.setPayeeId(smtEncasemenEntity.getDecCustomId());
						 	financialDetail.setStatus("0");
						 	this.save(financialDetail);
						 	
						 	//订单状态表记录
							String sql2 = "update smt_order_state set packing_state = ?,packing_time = ? where order_id = ?";
							this.executeSql(sql2, new Object[]{Constants_smt.SMT_ORDER_STATUS_BGYFS, DateUtils.formatDate(new Date(), "yyyy-MM-dd HH:mm:ss"), smtGroupDetailEntity.getOrderId()});
						 	/*	//回调商城接口
							SmtOrderEntity smtOrderEntity = this.get(SmtOrderEntity.class, smtGroupDetailEntity.getOrderId());
							if(smtOrderEntity!=null && StringUtil.isNotEmpty(smtOrderEntity.getNumber())){
								LinkedHashMap<String, Object> resultMap = new LinkedHashMap<String, Object>();
								resultMap.put("group_status", Constants_smt.SMT_ORDER_STATUS_ZHYTJ);
								resultMap.put("number", smtOrderEntity.getNumber());
								resultMap.put("msg", Constants_smt.getSmtOrderMapLabel(Constants_smt.SMT_ORDER_STATUS_ZHYTJ));
								SoapResponseUtil.responseSoap(JSON.toJSONString(resultMap), "TN0001");
							}*/
						}
					}
				}
				//记录到出口统计表
				String exportSql = 
						"SELECT\n" +
						"	smt_order.supplier,\n" +
						"	smt_order.goods_store,\n" +
						"	smt_encasemen.dec_custom_name,\n" +
						"	smt_order_detail.goods_name,\n" +
						"	concat(smt_goods.hs_code,smt_goods.extra_code) hs_code,\n" +
						"	smt_order_detail.goods_price,\n" +
						"	smt_order_detail.total_amount,\n" +
						"	smt_order_detail.goods_num,\n" +
						"	smt_goods.amount_unit,\n" +
						"  smt_decration_cus.export_port,\n" +
						"  smt_decration_cus.arrive_port,\n" +
						"  smt_decration_cus.dec_date\n" +
						"FROM\n" +
						"	smt_decration_cus\n" +
						"LEFT JOIN smt_encasemen ON smt_decration_cus.encase_id = smt_encasemen.id\n" +
						"LEFT JOIN smt_encasement_detail ON smt_encasemen.id = smt_encasement_detail.encase_id\n" +
						"LEFT JOIN smt_group ON smt_encasement_detail.group_id = smt_group.id\n" +
						"LEFT JOIN smt_group_detail ON smt_group.id = smt_group_detail.group_id\n" +
						"LEFT JOIN smt_order ON smt_group_detail.order_id = smt_order.id\n" +
						"LEFT JOIN smt_order_detail ON smt_order.id = smt_order_detail.order_id\n" +
						"LEFT JOIN smt_goods ON smt_order_detail.goods_id = smt_goods.id\n" +
						"WHERE\n" +
						"	smt_decration_cus.id = ?";
				List<Map<String, Object>> exportList = this.findForJdbc(exportSql, new Object[]{custom.get("id")});
				for (Map<String, Object> map : exportList) {
					String insertExportSql = "insert into smt_exports_data(id,supplier,trade_comp,decl_comp,goods_name,hs_code,price,total_price,goods_no,goods_unit,export_port,arrive_port,decl_date) values(?,?,?,?,?,?,?,?,?,?,?,?,?)";
					this.executeSql(insertExportSql, new Object[]{UUIDGenerator.generate(),map.get("supplier"),map.get("goods_store"),map.get("dec_custom_name"),map.get("goods_name"),map.get("hs_code"),map.get("goods_price"),map.get("total_amount"),map.get("goods_num"),map.get("amount_unit"),map.get("export_port"),map.get("arrive_port"),map.get("dec_date")});
				}
			}else if(!jsonObject.isNull("return")){
				if(opType.equals("A")){
					JSONArray jsonArray = jsonObject.getJSONArray("return");
					JSONObject otherMessage  = (JSONObject) jsonArray.get(0);
					String sql0 = "update smt_decration_cus set number = ?,cop_id = ?,status =-1 where id = ?";
					this.executeSql(sql0, new Object[]{otherMessage.get("seqNo"), otherMessage.get("copId"), custom.get("id")});
				}
				custom.put("status", "-1");
			}
		}
		return response;
	}
	
	/**
	 * 一次性录入报关单
	 * @param opType
	 * @param custom
	 * @return
	 */
	public String execAuto(String opType,Map<String, Object> custom,SmtMainInfoEntity smtMainInfo){
		String autoXmlStr = this.getAutoXmlStr(opType, custom, smtMainInfo);
		logger.info(DateUtils.getDate("yyyy-MM-dd HH:mm:ss")+" 报关单Request："+autoXmlStr);
		//字符串转文件
		/*String fileName = custom.get("create_name")+DateUtils.getDate("yyyyMMddHHmmss")+".log";
		StringToXML.string2File(customXmlStr, ResourceUtil.getConfigByName("xmlpath")+"8-custom/"+fileName);*/
		String response = this.subjectIntFaceFacade.sendDeclaration(ResourceUtil.getConfigByName("corpCode"),ResourceUtil.getConfigByName("corpName"),ResourceUtil.getConfigByName("loginCode"),ResourceUtil.getConfigByName("loginPassWord"), autoXmlStr);
		if(StringUtil.isNotEmpty(response)){
			JSONObject jsonObject =new JSONObject(response);
			//回执记录
			SmtReturnHistoryEntity t = new SmtReturnHistoryEntity();
			t.setReturnStatus(jsonObject.get("result").toString());
			t.setReturnMessage(response);
			t.setReturnTime(DateUtil.str2Date(DateUtils.date2Str(new Date(), DateUtils.datetimeFormat)));
			t.setYwid(smtMainInfo.getId());
			if((Integer)jsonObject.get("result")==1 && !jsonObject.isNull("return")){
				JSONArray jsonArray = jsonObject.getJSONArray("return");
				JSONObject otherMessage  = (JSONObject) jsonArray.get(0);
				String sql0 = "update smt_decration_cus set number = ?,cop_id = ?,status = 80002 where id = ?";
				this.executeSql(sql0, new Object[]{otherMessage.get("seqNo"), otherMessage.get("copId"), custom.get("id")});
				/*//资金明细
				String hql0 = "from SmtEncasementDetailEntity where 1 = 1 AND eNCASE_ID = ? ";
				List<SmtEncasementDetailEntity> smtEncasementDetailOldList = this.findHql(hql0, custom.get("encase_id"));
				SmtEncasemenEntity smtEncasemenEntity = this.get(SmtEncasemenEntity.class, custom.get("encase_id").toString());
				if(smtEncasementDetailOldList!=null && smtEncasementDetailOldList.size()>0){
					for (SmtEncasementDetailEntity smtEncasementDetailEntity : smtEncasementDetailOldList) {
						String hql1 = "from SmtGroupDetailEntity where 1 = 1 AND gROUP_ID = ? ";
						List<SmtGroupDetailEntity> smtGroupDetailOldList = this.findHql(hql1,smtEncasementDetailEntity.getGroupId());
						for (SmtGroupDetailEntity smtGroupDetailEntity : smtGroupDetailOldList) {
							//更新交易单状态
							String orderSql = "update smt_order set status = ? where id = ?";
							this.executeSql(orderSql, new Object[]{Constants_smt.SMT_ORDER_STATUS_YBG, smtGroupDetailEntity.getOrderId()});
							
							SmtOrderEntity order = this.get(SmtOrderEntity.class, smtGroupDetailEntity.getOrderId());
						 	SmtFinancialDetailEntity financialDetail = new SmtFinancialDetailEntity();
						 	financialDetail.setOrderId(order.getId());
						 	financialDetail.setOrderSn(order.getOrderSn());
						 	financialDetail.setPurpose("代理费用");
						 	financialDetail.setDetail("报关代理费用");
						 	financialDetail.setAmount(smtEncasemenEntity.getBgPrice());
						 	financialDetail.setPayer(order.getCreateName());
						 	financialDetail.setPayerId(order.getCreateBy());
						 	financialDetail.setPayee(smtEncasemenEntity.getDecCustomName());
						 	financialDetail.setPayeeId(smtEncasemenEntity.getDecCustomId());
						 	financialDetail.setStatus("0");
						 	this.save(financialDetail);
						 	
						 	//订单状态表记录
							String sql2 = "update smt_order_state set packing_state = ?,packing_time = ? where order_id = ?";
							this.executeSql(sql2, new Object[]{Constants_smt.SMT_ORDER_STATUS_BGYFS, DateUtils.formatDate(new Date(), "yyyy-MM-dd HH:mm:ss"), smtGroupDetailEntity.getOrderId()});
						 		//回调商城接口
							SmtOrderEntity smtOrderEntity = this.get(SmtOrderEntity.class, smtGroupDetailEntity.getOrderId());
							if(smtOrderEntity!=null && StringUtil.isNotEmpty(smtOrderEntity.getNumber())){
								LinkedHashMap<String, Object> resultMap = new LinkedHashMap<String, Object>();
								resultMap.put("group_status", Constants_smt.SMT_ORDER_STATUS_ZHYTJ);
								resultMap.put("number", smtOrderEntity.getNumber());
								resultMap.put("msg", Constants_smt.getSmtOrderMapLabel(Constants_smt.SMT_ORDER_STATUS_ZHYTJ));
								SoapResponseUtil.responseSoap(JSON.toJSONString(resultMap), "TN0001");
							}
						}
					}
				}
				//记录到出口统计表
				String exportSql = 
						"SELECT\n" +
						"	smt_order.supplier,\n" +
						"	smt_order.goods_store,\n" +
						"	smt_encasemen.dec_custom_name,\n" +
						"	smt_order_detail.goods_name,\n" +
						"	concat(smt_goods.hs_code,smt_goods.extra_code) hs_code,\n" +
						"	smt_order_detail.goods_price,\n" +
						"	smt_order_detail.total_amount,\n" +
						"	smt_order_detail.goods_num,\n" +
						"	smt_goods.amount_unit,\n" +
						"  smt_decration_cus.export_port,\n" +
						"  smt_decration_cus.arrive_port,\n" +
						"  smt_decration_cus.dec_date\n" +
						"FROM\n" +
						"	smt_decration_cus\n" +
						"LEFT JOIN smt_encasemen ON smt_decration_cus.encase_id = smt_encasemen.id\n" +
						"LEFT JOIN smt_encasement_detail ON smt_encasemen.id = smt_encasement_detail.encase_id\n" +
						"LEFT JOIN smt_group ON smt_encasement_detail.group_id = smt_group.id\n" +
						"LEFT JOIN smt_group_detail ON smt_group.id = smt_group_detail.group_id\n" +
						"LEFT JOIN smt_order ON smt_group_detail.order_id = smt_order.id\n" +
						"LEFT JOIN smt_order_detail ON smt_order.id = smt_order_detail.order_id\n" +
						"LEFT JOIN smt_goods ON smt_order_detail.goods_id = smt_goods.id\n" +
						"WHERE\n" +
						"	smt_decration_cus.id = ?";
				List<Map<String, Object>> exportList = this.findForJdbc(exportSql, new Object[]{custom.get("id")});
				for (Map<String, Object> map : exportList) {
					String insertExportSql = "insert into smt_exports_data(id,supplier,trade_comp,decl_comp,goods_name,hs_code,price,total_price,goods_no,goods_unit,export_port,arrive_port,decl_date) values(?,?,?,?,?,?,?,?,?,?,?,?,?)";
					this.executeSql(insertExportSql, new Object[]{UUIDGenerator.generate(),map.get("supplier"),map.get("goods_store"),map.get("dec_custom_name"),map.get("goods_name"),map.get("hs_code"),map.get("goods_price"),map.get("total_amount"),map.get("goods_num"),map.get("amount_unit"),map.get("export_port"),map.get("arrive_port"),map.get("dec_date")});
				}*/
			}
		}
		logger.info(DateUtils.getDate("yyyy-MM-dd HH:mm:ss")+" 报关单Response："+response);
		return response;
	}
	
	
	/**
	 * 电子随附单据
	 * @param smtDecrationCus
	 * @param smtCert
	 * @return
	 */
	public String execCert(SmtDecrationCusEntity smtDecrationCus, SmtCertEntity smtCert){
		String certXmlStr = this.getCertXmlStr(smtDecrationCus, smtCert);
		logger.info(DateUtils.getDate("yyyy-MM-dd HH:mm:ss")+" 电子随附单据Request："+certXmlStr);
		String response = this.subjectIntFaceFacade.sendDeclaration(ResourceUtil.getConfigByName("corpCode"),ResourceUtil.getConfigByName("corpName"),ResourceUtil.getConfigByName("loginCode"),ResourceUtil.getConfigByName("loginPassWord"), certXmlStr);
		logger.info(DateUtils.getDate("yyyy-MM-dd HH:mm:ss")+" 电子随附单据Response："+response);
		return response;
	}
	/**
	 * 报检单
	 * @param opType
	 * @param decl
	 * @return
	 * @throws Exception 
	 */
	public String execDecl(String opType,Map<String, Object> decl) throws Exception{
		String declXmlStr = this.getDeclXmlStr(opType, decl);
		logger.info(DateUtils.getDate("yyyy-MM-dd HH:mm:ss")+" 报检单Request："+declXmlStr);
		//字符串转文件
		String fileName = decl.get("create_name")+DateUtils.getDate("yyyyMMddHHmmss")+".log";
		StringToXML.string2File(declXmlStr, ResourceUtil.getConfigByName("xmlpath")+"9-decl/"+fileName);
		String response = this.subjectIntFaceFacade.sendDeclaration(ResourceUtil.getConfigByName("corpCode"),ResourceUtil.getConfigByName("corpName"),ResourceUtil.getConfigByName("loginCode"),ResourceUtil.getConfigByName("loginPassWord"), declXmlStr);
		logger.info(DateUtils.getDate("yyyy-MM-dd HH:mm:ss")+" 报检单response："+response);
		if(StringUtil.isNotEmpty(response)){
			JSONObject jsonObject =new JSONObject(response);
			if((Integer)jsonObject.get("result")==1 && !jsonObject.isNull("return")){
				JSONArray jsonArray = jsonObject.getJSONArray("return");
				JSONObject otherMessage  = (JSONObject) jsonArray.get(0);
				String sql0 = "update smt_decration_ciq set relaid = ?,status = 90002 where id = ?";
				this.executeSql(sql0, new Object[]{otherMessage.get("entDeclNo"), decl.get("id")});
				decl.put("status",Constants_smt.SMT_ORDER_STATUS_BJYFS);
				decl.put("ciq_no", otherMessage.get("entDeclNo"));
				//资金明细
				String hql0 = "from SmtEncasementDetailEntity where 1 = 1 AND eNCASE_ID = ? ";
				List<SmtEncasementDetailEntity> smtEncasementDetailOldList = this.findHql(hql0, decl.get("packing_id"));
				SmtEncasemenEntity smtEncasemenEntity = this.get(SmtEncasemenEntity.class, decl.get("packing_id").toString());
				if(smtEncasementDetailOldList!=null && smtEncasementDetailOldList.size()>0){
					for (SmtEncasementDetailEntity smtEncasementDetailEntity : smtEncasementDetailOldList) {
						String hql1 = "from SmtGroupDetailEntity where 1 = 1 AND gROUP_ID = ? ";
						List<SmtGroupDetailEntity> smtGroupDetailOldList = this.findHql(hql1,smtEncasementDetailEntity.getGroupId());
						for (SmtGroupDetailEntity smtGroupDetailEntity : smtGroupDetailOldList) {
							SmtOrderEntity order = this.get(SmtOrderEntity.class, smtGroupDetailEntity.getOrderId());
						 	SmtFinancialDetailEntity financialDetail = new SmtFinancialDetailEntity();
						 	financialDetail.setOrderId(order.getId());
						 	financialDetail.setOrderSn(order.getOrderSn());
						 	financialDetail.setPurpose("代理费用");
						 	financialDetail.setDetail("报检代理费用");
						 	financialDetail.setAmount(smtEncasemenEntity.getBjPrice());
						 	financialDetail.setPayer(order.getCreateName());
						 	financialDetail.setPayerId(order.getCreateBy());
						 	financialDetail.setPayee(smtEncasemenEntity.getDecQualityName());
						 	financialDetail.setPayeeId(smtEncasemenEntity.getDecQualityId());
						 	financialDetail.setStatus("0");
						 	this.save(financialDetail);
						 	
						 	//订单状态表记录
							String sql2 = "update smt_order_state set packing_state = ?,packing_time = ? where order_id = ?";
							this.executeSql(sql2, new Object[]{Constants_smt.SMT_ORDER_STATUS_BJYFS, DateUtils.formatDate(new Date(), "yyyy-MM-dd HH:mm:ss"), smtGroupDetailEntity.getOrderId()});
						 	/*	//回调商城接口
							SmtOrderEntity smtOrderEntity = this.get(SmtOrderEntity.class, smtGroupDetailEntity.getOrderId());
							if(smtOrderEntity!=null && StringUtil.isNotEmpty(smtOrderEntity.getNumber())){
								LinkedHashMap<String, Object> resultMap = new LinkedHashMap<String, Object>();
								resultMap.put("group_status", Constants_smt.SMT_ORDER_STATUS_ZHYTJ);
								resultMap.put("number", smtOrderEntity.getNumber());
								resultMap.put("msg", Constants_smt.getSmtOrderMapLabel(Constants_smt.SMT_ORDER_STATUS_ZHYTJ));
								SoapResponseUtil.responseSoap(JSON.toJSONString(resultMap), "TN0001");
							}*/
						}
					}
				}
			}
		}
		return response;
	}
	
	/**
	 * 代理出口货物证明
	 * @param opType
	 * @param goodsCert
	 * @return
	 */
	public void execGoodsCert(String opType, SmtMarketUserEntity userEntity) throws Exception{
		String goodsCertXmlStr = this.getGoodsCertXmlStr(opType, userEntity.getRegNum());
		logger.info(DateUtils.getDate("yyyy-MM-dd HH:mm:ss")+" 代理出口货物证明Request："+goodsCertXmlStr);
		String response = this.subjectIntFaceFacade.sendDeclaration(ResourceUtil.getConfigByName("corpCode"),ResourceUtil.getConfigByName("corpName"),ResourceUtil.getConfigByName("loginCode"),ResourceUtil.getConfigByName("loginPassWord"), goodsCertXmlStr);
		logger.info(DateUtils.getDate("yyyy-MM-dd HH:mm:ss")+" 代理出口货物证明Response："+response);
		if(!StringUtils.isNotEmpty(response)){
			throw new Exception("市场采购系统未返回回执报文，请联系管理员！");
		}
		//Map maps = (Map)JSON.parse(response); 
		this.findGoodsCert(userEntity);
	}
	
	/**
	 * 免税申报
	 * @param opType
	 * @param freeList
	 * @param senderMap
	 * @throws Exception 
	 */
	public void execFree(String opType, SmtBusinessEntity bus) throws Exception{
		//1、生成免税申报单
		String freeXmlStr = this.getFreeXmlStr(opType,bus.getCreateBy().toUpperCase(), bus.getRegNum());
		logger.info(DateUtils.getDate("yyyy-MM-dd HH:mm:ss")+" 免税申报单生成请求："+freeXmlStr);
		String response = this.subjectIntFaceFacade.sendDeclaration(ResourceUtil.getConfigByName("corpCode"),ResourceUtil.getConfigByName("corpName"),ResourceUtil.getConfigByName("loginCode"),ResourceUtil.getConfigByName("loginPassWord"), freeXmlStr);
		logger.info(DateUtils.getDate("yyyy-MM-dd HH:mm:ss")+" 免税申报单生成结果："+response);
		//2、同步免税申报单
		if(!StringUtils.isNotEmpty(response)){
			throw new Exception("市场采购系统未返回回执报文，请联系管理员！");
		}else{
			SmtMarketUserEntity userEntity =smtProveService.findUniqueByProperty(SmtMarketUserEntity.class, "createBy", ResourceUtil.getSessionUserName().getUserName());
			if(userEntity!=null){
				this.findFreetax(userEntity);
			}else{
				throw new Exception("请完善委托圣贸通代理信息！");
			}
			
		}
	}
	
 	/**
	 * 商户备案报文
	 * @param opType
	 * @param sender
	 * @param data
	 * @return
	 */
	private String getCompXmlStr(String opType, Map<String,Object> data){
		int num=(int)(Math.random()*9000)+1000;
		String messageId = "COMP_"+data.get("reg_num")+"_"+DateUtils.getDate("yyyyMMddHHmmss")+""+num;
		String characters = "1";
		if(data.get("company_matype")!=null){
			if(data.get("company_matype").toString().indexOf("a")!=-1){
				characters += ",9,2";
			}
			if(data.get("company_matype").toString().indexOf("b")!=-1){
				characters += ",5";
			}
			if(data.get("company_matype").toString().indexOf("c")!=-1){
				characters += ",4";
			}	
		}
		String compXmlStr =
				"<?xml version='1.0' encoding='utf-8'?>\n" +
				"<SubjectInfo>\n" +
				"<Head>\n" +
				"   <MessageId>"+messageId+"</MessageId>\n" +
				"	<MessageType>COMP</MessageType>\n" +
				"	<Sender>"+data.get("reg_num")+"</Sender>\n" +
				"	<Receiver>GZSW</Receiver>\n" +
				"	<opType>"+opType+"</opType>\n" +
				"</Head>\n" +
				"<Declaration>\n" +
				"<DsqCorp>\n" +
				"	<orgId>"+(data.get("org_id")==null?"":data.get("org_id"))+"</orgId>\n" +
				"	<sgsRegCode>"+(data.get("reg_num")==null?"":data.get("reg_num").toString().trim())+"</sgsRegCode>\n" +
				"	<corpCname>"+(data.get("cn_name")==null?"":data.get("cn_name").toString().trim())+"</corpCname>\n" +
				"	<corpEname>"+(data.get("en_name")==null?"":data.get("en_name"))+"</corpEname>\n" +
				"	<corpShortName>"+(data.get("company")==null?"":data.get("company"))+"</corpShortName>\n" +
				"	<corpOrgCode>"+(data.get("reg_num")==null?"":data.get("reg_num").toString().trim())+"</corpOrgCode>\n" +
				"	<locationfCode>"+(data.get("locationf_code")==null?"":data.get("locationf_code").toString().trim())+"</locationfCode>\n" +
				"	<corpType>1</corpType>\n" +
				"	<characters>"+characters+"</characters>\n" +
				"	<legalName>"+(data.get("corporation")==null?"":data.get("corporation"))+"</legalName>\n" +
				"	<identCode>"+(data.get("cert_type")==null?"":data.get("cert_type"))+"</identCode>\n" +
				"	<legalIdCode>"+(data.get("cert_num")==null?"":data.get("cert_num"))+"</legalIdCode>\n" +
				"	<contractMan>"+(data.get("contacts")==null?"":data.get("contacts"))+"</contractMan>\n" +
				"	<loginUserName>"+(data.get("create_by")==null?"":data.get("create_by"))+"</loginUserName>\n" +
				"	<cellphoneNo>"+(data.get("phone")==null?"":data.get("phone"))+"</cellphoneNo>\n" +
				"	<email>"+(data.get("email")==null?"":data.get("email"))+"</email>\n" +
				"	<faxNo>"+(data.get("fax")==null?"":data.get("fax"))+"</faxNo>\n" +
				"	<postCode>"+(data.get("postcode")==null?"":data.get("postcode"))+"</postCode>\n" +
				"	<caddress>"+(data.get("bus_place")==null?"":data.get("bus_place"))+"</caddress>\n" +
				"	<regMoney>"+(data.get("capital")==null?"":data.get("capital"))+"</regMoney>\n" +
				"	<companyType>"+(data.get("com_type")==null?"":data.get("com_type"))+"</companyType>\n" +
				"	<scope>"+(data.get("bus_scope")==null?"":data.get("bus_scope"))+"</scope>\n" +
				"	<industryType>"+(data.get("bus_type")==null?"":data.get("bus_type"))+"</industryType>\n" +
				"	<taxIdentCode>"+(data.get("taxer_num")==null?"":data.get("taxer_num"))+"</taxIdentCode>\n" +
				"	<corpTaxType>"+(data.get("tax_type")==null?"":data.get("tax_type"))+"</corpTaxType>\n" +
				"	<customCode>"+(data.get("cus_code")==null?"":data.get("cus_code"))+"</customCode>\n" +
				"	<declCode>"+(data.get("ciq_code")==null?"":data.get("ciq_code"))+"</declCode>\n"+
				"   <ioCorpCode>"+(data.get("iae_code")==null?"":data.get("iae_code"))+"</ioCorpCode>\n" +
				"	<creditLevel>"+(data.get("credit_level")==null?"":data.get("credit_level"))+"</creditLevel>\n" +
				"	<validateBeg>"+(data.get("vail_time")==null?"":DateUtils.dateformat(data.get("vail_time").toString(), "yyyy-MM-dd"))+"</validateBeg>\n" +
				"	<validateEnd>"+(data.get("avail_time")==null?"":DateUtils.dateformat(data.get("avail_time").toString(), "yyyy-MM-dd"))+"</validateEnd>\n" +
				"	<regDate>"+(data.get("issuing_time")==null?"":DateUtils.dateformat(data.get("issuing_time").toString(), "yyyy-MM-dd"))+"</regDate>\n" +
				"	<openDate></openDate>\n" +
				"	<wisdomCheckNo></wisdomCheckNo>\n" +
				"	<resCountry></resCountry>\n" +
				"	<orgAddress>"+(data.get("company_addr")==null?"":data.get("company_addr"))+"</orgAddress>\n" +
				"	<finvest1></finvest1>\n" +
				"	<finvest2></finvest2>\n" +
				"	<finvest3></finvest3>\n" +
				"	<finvest4></finvest4>\n" +
				"	<finvest5></finvest5>\n" +
				"	<economyType></economyType>\n" +
				"	<isSpecialCorp></isSpecialCorp>\n" +
				"	<specialCorpType></specialCorpType>\n" +
				"	<period></period>\n" +
				"	<investMoneyKind></investMoneyKind>\n" +
				"	<fregMoneyDollar></fregMoneyDollar>\n" +
				"	<investSumMoney></investSumMoney>\n" +
				"	<rmbInvestSumMoney></rmbInvestSumMoney>\n" +
				"	<finvestSumMoneyDollar></finvestSumMoneyDollar>\n" +
				"	<orgType></orgType>\n" +
				"	<regionCode>"+(data.get("region_code")==null?"":data.get("region_code").toString().trim())+"</regionCode>\n" +
				"	<remark>"+(data.get("company_note")==null?"":data.get("company_note"))+"</remark>\n" +
				"</DsqCorp>\n" +
				"</Declaration>\n" +
				"</SubjectInfo>";
		return compXmlStr;
	}
	
	/**
	 * 企业备案报文
	 * @param opType
	 * @param sender
	 * @param data
	 * @return
	 */
	private String getBusinessXmlStr(String opType, Map<String,Object> data){
		int num=(int)(Math.random()*9000)+1000;
		String messageId = "COMP_"+data.get("reg_num")+"_"+DateUtils.getDate("yyyyMMddHHmmss")+""+num;
		String characters = "";
		String taxAuthoritiesCode="";
		if(data.get("company_matype").toString().indexOf("1")!=-1){
			characters += "9,";
			taxAuthoritiesCode="<taxAuthoritiesCode>"+(data.get("authorities_code"))+"</taxAuthoritiesCode>\n";
		}
		if(data.get("company_matype").toString().indexOf("2")!=-1){
			characters += "2,";
		}
		if(data.get("company_matype").toString().indexOf("3")!=-1){
			characters += "5,";
		}
		if(data.get("company_matype").toString().indexOf("4")!=-1){
			characters += "4";
		}	
		String compXmlStr =
				"<?xml version='1.0' encoding='utf-8'?>\n" +
				"<SubjectInfo>\n" +
				"<Head>\n" +
				"   <MessageId>"+messageId+"</MessageId>\n" +
				"	<MessageType>COMP</MessageType>\n" +
				"	<Sender>"+data.get("reg_num")+"</Sender>\n" +
				"	<Receiver>GZSW</Receiver>\n" +
				"	<opType>"+opType+"</opType>\n" +
				"</Head>\n" +
				"<Declaration>\n" +
				"<DsqCorp>\n" +
				"	<orgId>"+(data.get("org_id")==null?"":data.get("org_id"))+"</orgId>\n" +
				"	<sgsRegCode>"+(data.get("reg_num")==null?"":data.get("reg_num").toString().trim())+"</sgsRegCode>\n" +
				"	<regionCode>"+(data.get("region_code"))+"</regionCode>\n" +taxAuthoritiesCode+
				"	<locationfCode>"+(data.get("locationf_code"))+"</locationfCode>\n" +
				"	<corpCname>"+(data.get("cn_name")==null?"":data.get("cn_name").toString().trim())+"</corpCname>\n" +
				"	<corpEname>"+(data.get("en_name")==null?"":data.get("en_name"))+"</corpEname>\n" +
				"	<corpShortName>"+(data.get("company")==null?"":data.get("company"))+"</corpShortName>\n" +
				"	<corpOrgCode>"+(data.get("reg_num")==null?"":data.get("reg_num").toString().trim())+"</corpOrgCode>\n" +
				"	<corpType>2</corpType>\n" +
				"	<characters>"+characters+"</characters>\n" +
				"	<legalName>"+(data.get("corporation")==null?"":data.get("corporation"))+"</legalName>\n" +
				"	<identCode>"+(data.get("cert_type")==null?"":data.get("cert_type"))+"</identCode>\n" +
				"	<legalIdCode>"+(data.get("cert_num")==null?"":data.get("cert_num"))+"</legalIdCode>\n" +
				"	<contractMan>"+(data.get("contacts")==null?"":data.get("contacts"))+"</contractMan>\n" +
				"	<loginUserName>"+(data.get("create_by")==null?"":data.get("create_by"))+"</loginUserName>\n" +
				"	<cellphoneNo>"+(data.get("phone")==null?"":data.get("phone"))+"</cellphoneNo>\n" +
				"	<email>"+(data.get("email")==null?"":data.get("email"))+"</email>\n" +
				"	<faxNo>"+(data.get("fax")==null?"":data.get("fax"))+"</faxNo>\n" +
				"	<postCode>"+(data.get("postcode")==null?"":data.get("postcode"))+"</postCode>\n" +
				"	<caddress>"+(data.get("bus_place")==null?"":data.get("bus_place"))+"</caddress>\n" +
				"	<regMoney>"+(data.get("capital")==null?"":data.get("capital"))+"</regMoney>\n" +
				"	<companyType>"+(data.get("com_type")==null?"":data.get("com_type"))+"</companyType>\n" +
				"	<scope>"+(data.get("bus_scope")==null?"":data.get("bus_scope"))+"</scope>\n" +
				"	<industryType>"+(data.get("bus_type")==null?"":data.get("bus_type"))+"</industryType>\n" +
				"	<taxIdentCode>"+(data.get("taxer_num")==null?"":data.get("taxer_num"))+"</taxIdentCode>\n" +
				"	<corpTaxType>"+(data.get("tax_type")==null?"":data.get("tax_type"))+"</corpTaxType>\n" +
				"	<customCode>"+(data.get("cus_code")==null?"":data.get("cus_code"))+"</customCode>\n" +
				"	<declCode>"+(data.get("ciq_code")==null?"":data.get("ciq_code"))+"</declCode>\n"+
				"   <ioCorpCode>"+(data.get("iae_code")==null?"":data.get("iae_code"))+"</ioCorpCode>\n" +
				"	<creditLevel>"+(data.get("credit_level")==null?"":data.get("credit_level"))+"</creditLevel>\n" +
				"	<validateBeg>"+(data.get("vail_time")==null?"":DateUtils.dateformat(data.get("vail_time").toString(), "yyyy-MM-dd"))+"</validateBeg>\n" +
				"	<validateEnd>"+(data.get("avail_time")==null?"":DateUtils.dateformat(data.get("avail_time").toString(), "yyyy-MM-dd"))+"</validateEnd>\n" +
				"	<regDate>"+(data.get("issuing_time")==null?"":DateUtils.dateformat(data.get("issuing_time").toString(), "yyyy-MM-dd"))+"</regDate>\n" +
				"	<openDate></openDate>\n" +
				"	<wisdomCheckNo></wisdomCheckNo>\n" +
				"	<resCountry></resCountry>\n" +
				"	<orgAddress>"+(data.get("company_addr")==null?"":data.get("company_addr"))+"</orgAddress>\n" +
				"	<finvest1></finvest1>\n" +
				"	<finvest2></finvest2>\n" +
				"	<finvest3></finvest3>\n" +
				"	<finvest4></finvest4>\n" +
				"	<finvest5></finvest5>\n" +
				"	<economyType></economyType>\n" +
				"	<isSpecialCorp></isSpecialCorp>\n" +
				"	<specialCorpType></specialCorpType>\n" +
				"	<period></period>\n" +
				"	<investMoneyKind></investMoneyKind>\n" +
				"	<fregMoneyDollar></fregMoneyDollar>\n" +
				"	<investSumMoney></investSumMoney>\n" +
				"	<rmbInvestSumMoney></rmbInvestSumMoney>\n" +
				"	<finvestSumMoneyDollar></finvestSumMoneyDollar>\n" +
				"	<orgType></orgType>\n" +
				"	<remark>"+(data.get("company_note")==null?"":data.get("company_note"))+"</remark>\n" +
				"</DsqCorp>\n" +
				"</Declaration>\n" +
				"</SubjectInfo>";
		return compXmlStr;
	}
	
	/**
	 * 采购商备案登记报文
	 * @param opType
	 * @param sender
	 * @param data
	 * @return
	 */
	public String getBuyerXmlStr(String opType, Map<String, Object> map){
		int num=(int)(Math.random()*9000)+1000;
		String messageId = "BUYER_"+map.get("create_org")+"_"+DateUtils.getDate("yyyyMMddHHmmss")+""+num;
		String buyerXmlStr = 
				"<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
				"<SubjectInfo>\n" +
				"<Head>\n" +
				"   <MessageId>"+messageId+"</MessageId>\n" +
				"	<MessageType>BUYER</MessageType>\n" +
				"	<Sender>"+map.get("create_org")+"</Sender>\n" +
				"	<Receiver>GZSW</Receiver>\n" +
				"	<opType>"+opType+"</opType>\n" +
				"</Head>\n" +
				"<Declaration>\n" +
				"<BuyerList>\n" +
				"<Buyer>\n" +
				"   <corpCode>"+(map.get("cert_num")==null?"":map.get("cert_num"))+"</corpCode>\n" +
				"   <corpCname>"+(map.get("com_name")==null?"":map.get("com_name"))+"</corpCname>\n" +
				"   <companyType>"+(map.get("com_type")==null?"":map.get("com_type"))+"</companyType>\n" +
				"   <contractMan>"+(map.get("contacts")==null?"":map.get("contacts"))+"</contractMan>\n" +
				"   <finvest1>"+(map.get("country")==null?"":map.get("country"))+"</finvest1>\n" +
				"   <telno>"+(map.get("phone")==null?"":map.get("phone"))+"</telno>\n" +
				"   <address>"+(map.get("com_address")==null?"":map.get("com_address"))+"</address>\n" +
				"   <createOrg>"+(map.get("create_org")==null?"":map.get("create_org"))+"</createOrg>\n" +
				"</Buyer>\n" +
				"</BuyerList>\n" +
				"</Declaration>\n" +
				"</SubjectInfo>";
		return buyerXmlStr;
	}
	
	/**
	 * 生产商备案报文
	 * @param opType
	 * @param sender
	 * @param data
	 * @return
	 */
	public String getMafXmlStr(String opType, Map<String, Object> map){
		int num=(int)(Math.random()*9000)+1000;
		String messageId = "MAF_"+map.get("create_org")+"_"+DateUtils.getDate("yyyyMMddHHmmss")+""+num;
		String mafXmlStr = 
				"<?xml version='1.0' encoding='utf-8'?>\n" +
				"<SubjectInfo>\n" +
				"<Head>\n" +
				"<MessageId>"+messageId+"</MessageId>\n" +
				"	<MessageType>MAF</MessageType>\n" +
				"	<Sender>"+map.get("create_org")+"</Sender>\n" +
				"	<Receiver>GZSW</Receiver>\n" +
				"	<opType>"+opType+"</opType>\n" +
				"</Head>\n" +
				"<Declaration>\n" +
				"<ManufacturerList>\n" +
				"<Manufacturer>\n" +
				"	<corpCode>"+(map.get("org_code")==null?"":map.get("org_code"))+"</corpCode>\n" +
				"   <corpCname>"+(map.get("cn_name")==null?"":map.get("cn_name"))+"</corpCname>\n" +
				"   <corpEname>"+(map.get("en_name")==null?"":map.get("en_name"))+"</corpEname>\n" +
				"   <companyType>"+(map.get("com_type")==null?"":map.get("com_type"))+"</companyType>\n" +
				"   <contractMan>"+(map.get("contacts")==null?"":map.get("contacts"))+"</contractMan>\n" +
				"   <identCode>"+(map.get("cert_num")==null?"":map.get("cert_num"))+"</identCode>\n" +
				"   <telno>"+(map.get("phone")==null?"":map.get("phone"))+"</telno>\n" +
				"	<caddress>"+(map.get("com_address")==null?"":map.get("com_address"))+"</caddress>\n" +
				"   <createOrg>"+(map.get("create_org")==null?"":map.get("create_org"))+"</createOrg>\n" +
				"</Manufacturer>\n" +
				"</ManufacturerList>\n" +
				"</Declaration>\n" +
				"</SubjectInfo>";
		return mafXmlStr;
	}
	
	/**
	 * 商品备案报文
	 * @param opType
	 * @param sender
	 * @param data
	 * @return
	 */
	public String getGoodsXmlStr(String opType, Map<String, Object> map){
		int num=(int)(Math.random()*9000)+1000;
		String messageId = "GOODS_"+map.get("create_org")+"_"+DateUtils.getDate("yyyyMMddHHmmss")+""+num;
		String goodsXmlStr = 
				"<?xml version=\"1.0\" encoding=\"utf-8\"?>\n" +
				"<SubjectInfo>\n" +
				"<Head>\n" +
				"   <MessageId>"+messageId+"</MessageId>\n" +
				"	<MessageType>GOODS</MessageType>\n" +
				"	<Sender>"+map.get("create_org")+"</Sender>\n" +
				"	<Receiver>GZSW</Receiver>\n" +
				"	<opType>"+opType+"</opType>\n" +
				"</Head>\n" +
				"<Declaration>\n" +
				"<GoodList>\n"+
				"<Good>\n" +
				"	<createOrg>"+(map.get("create_org")==null?"":map.get("create_org"))+"</createOrg>\n" +
				"   <goodsCname>"+(map.get("goods_name")==null?"":map.get("goods_name"))+"</goodsCname>\n" +
				"   <goodsEname>"+(map.get("goods_en_name")==null?"":map.get("goods_en_name"))+"</goodsEname>\n" +
				"   <goodsId>"+(map.get("id")==null?"":map.get("id"))+"</goodsId>\n" +
				"   <goodsImage></goodsImage>\n" +
				"   <isBrand>"+(map.get("is_authorized")==null?"":map.get("is_authorized"))+"</isBrand>\n" +
				"   <model>"+(map.get("spec")==null?"":map.get("spec"))+"</model>\n" +
				"   <producer>"+(map.get("producer")==null?"":map.get("producer"))+"</producer>\n" +
				"   <qunit>"+(map.get("weight_unit_code")==null?"":map.get("weight_unit_code"))+"</qunit>\n" +
				"   <wunit>"+(map.get("second_unit_code")==null?"":map.get("second_unit_code"))+"</wunit>\n" +
				"	<cunit>"+(map.get("amount_unit_code")==null?"":map.get("amount_unit_code"))+"</cunit>\n" +
				"   <remark>"+(map.get("remark")==null?"":map.get("remark"))+"</remark>\n" +
				"   <cBrand>"+(map.get("cn_brand")==null?"":map.get("cn_brand"))+"</cBrand>\n" +
				"   <eBrand>"+(map.get("en_brand")==null?"":map.get("en_brand"))+"</eBrand>\n" +
				"	<goodsCode>"+(map.get("cg_goods_code")==null?"":map.get("cg_goods_code"))+"</goodsCode>\n" +
				"	<reason></reason>\n" +
				"	<hsCode>"+(map.get("hs_code")==null?"":map.get("hs_code"))+"</hsCode>\n" +
				"	<hsCodeS>"+(map.get("extra_code")==null?"":map.get("extra_code"))+"</hsCodeS>\n" +
				"	<corpOwnerCode>"+(map.get("self_num")==null?"":map.get("self_num"))+"</corpOwnerCode>\n" +
				"	<goodsType>"+(map.get("goods_classify")==null?"":map.get("goods_classify"))+"</goodsType>\n" +
				"</Good>\n" +
				"</GoodList>\n" +
				"</Declaration>\n" +
				"</SubjectInfo>";
		return goodsXmlStr;
	}
	
	/**
	 * 交易单报文
	 * @param opType
	 * @param sender
	 * @param tradeList
	 * @return
	 */
	private String getTradeXmlStr(String opType, Map<String, Object> trade){
		int num=(int)(Math.random()*9000)+1000;
		String messageId = "TRAD_"+DateUtils.getDate("yyyyMMddHHmmss")+""+num;
		String tradeXmlStr = 
				"<?xml version='1.0' encoding='UTF-8'?>\n" +
				"<SubjectInfo>\n" +
				"<Head>\n" +
				"	<MessageId>"+messageId+"</MessageId>\n" +
				"	<MessageType>TRAD</MessageType>\n" +
				"	<Sender>"+trade.get("solder")+"</Sender>\n" +
				"	<Receiver>GZSW</Receiver>\n" +
				"	<opType>"+opType+"</opType>\n" +
				"</Head>\n" +
				"<Declaration>\n" +
				"<TradeList>\n" +
				"<Trade>\n" +
				"	<applyWay>"+(trade.get("apply_way")==null?"":trade.get("apply_way"))+"</applyWay>\n" +
				"	<buyer>"+(trade.get("buyer")==null?"":trade.get("buyer"))+"</buyer>\n" +
				"	<solder>"+(trade.get("solder")==null?"":trade.get("solder"))+"</solder>\n" +
				"	<proxyer>"+(trade.get("proxyer")==null?"":trade.get("proxyer"))+"</proxyer>\n" +
				"	<isAgencyReceipt>"+(trade.get("is_agency_receipt")==null?"":trade.get("is_agency_receipt"))+"</isAgencyReceipt>\n" +
				"	<rmbMoney>"+(trade.get("rmb_money")==null?"":trade.get("rmb_money"))+"</rmbMoney>\n" +
				"	<tradingId>"+(trade.get("id")==null?"":trade.get("id"))+"</tradingId>\n" +
				"	<tradingNo></tradingNo>\n" +
				"	<createOrg>"+(trade.get("solder")==null?"":trade.get("solder"))+"</createOrg>\n" +
				"	<status>1</status>\n" +
				"	<remark>"+(trade.get("note")==null?"":trade.get("note"))+"</remark>\n" +
				"	<TradeDetailList>\n";
		    String sql =
		    		"SELECT\n" +
		    		"	smt_order_detail.*, smt_goods.cg_goods_code\n" +
		    		"FROM\n" +
		    		"	smt_order_detail,\n" +
		    		"	smt_goods\n" +
		    		"WHERE\n" +
		    		"	smt_order_detail.goods_id in (smt_goods.cg_goods_code,smt_goods.id)\n" +
		    		"AND smt_order_detail.order_id = ?";
			List<Map<String, Object>> tradeDetailList = this.findForJdbc(sql, new Object[]{trade.get("id")});
			for (Map<String, Object> tradeDetail : tradeDetailList) {
				tradeXmlStr +=
				"	<TradeDetail>\n" +
				"		<goodsCode>"+(tradeDetail.get("cg_goods_code")==null?"":tradeDetail.get("cg_goods_code"))+"</goodsCode>\n" +
				"		<quantity>"+(tradeDetail.get("legal_num")==null?"":tradeDetail.get("legal_num"))+"</quantity>\n" +
				"		<cAmount>"+(tradeDetail.get("goods_num")==null?"":tradeDetail.get("goods_num"))+"</cAmount>\n" +
				"		<weight>"+(tradeDetail.get("second_num")==null?"":tradeDetail.get("second_num"))+"</weight>\n" +
				"		<price>"+(tradeDetail.get("goods_price")==null?"":tradeDetail.get("goods_price"))+"</price>\n" +
				"		<totalPrice>"+(tradeDetail.get("total_amount")==null?"":tradeDetail.get("total_amount"))+"</totalPrice>\n" +
				"		<tradeCurr>"+(tradeDetail.get("currency")==null?"":tradeDetail.get("currency"))+"</tradeCurr>\n" +
				"		<tradeDetailIdReturn></tradeDetailIdReturn>\n" +
				"		<tradeDetailId>"+(tradeDetail.get("id")==null?"":tradeDetail.get("id"))+"</tradeDetailId>\n" +
				"	</TradeDetail>\n";
			}
			tradeXmlStr += 
				"	</TradeDetailList>\n" +
				"</Trade>\n" +
				"</TradeList>\n" +
				"</Declaration>\n" +
				"</SubjectInfo>";
		return tradeXmlStr;
	}
	
	
	/**
	 * 组货单报文
	 * @param opType
	 * @param sender
	 * @param data
	 * @return
	 */
	public String getCombXmlStr(String opType, Map<String, Object> map){
		int num=(int)(Math.random()*9000)+1000;
		String messageId = "COMB_"+map.get("create_org")+"_"+DateUtils.getDate("yyyyMMddHHmmss")+""+num;
		String combXmlStr =
				"<?xml version='1.0' encoding='utf-8'?>\n" +
				"<SubjectInfo>\n" +
				"<Head>\n" +
				"<MessageId>"+messageId+"</MessageId>\n" +
				"	<MessageType>COMB</MessageType>\n" +
				"	<Sender>"+map.get("create_org")+"</Sender>\n" +
				"	<Receiver>GZSW</Receiver>\n" +
				"	<opType>"+opType+"</opType>\n" +
				"</Head>\n" +
				"<Declaration>\n" +
				"<ComboList>\n" +
				"<Combo>\n" +
				"	<destCountry>"+(map.get("dest_country")==null?"":map.get("dest_country"))+"</destCountry>\n" +
				"   <carNo>"+(map.get("plate_num")==null?"":map.get("plate_num"))+"</carNo>\n" +
				"   <proxyer>"+(map.get("proxyer")==null?"":map.get("proxyer"))+"</proxyer>\n" +
				"   <remark>"+(map.get("note")==null?"":map.get("note"))+"</remark>\n" +
				"   <orderCode></orderCode>\n" +
				"   <comboId>"+(map.get("id")==null?"":map.get("id"))+"</comboId>\n" +
				"   <createOrg>"+(map.get("create_org")==null?"":map.get("create_org"))+"</createOrg>\n" +
				"	<reason></reason>\n" +
				"   <ComboDetailList>\n";
			List<Map<String, Object>> comboDetailList = this.findForJdbc("select smt_order.order_sn from smt_group_detail,smt_order where smt_group_detail.order_id in(smt_order.id,smt_order.order_sn) and smt_group_detail.group_id = ?", new Object[]{map.get("id")});
			for (Map<String, Object> comboDetail : comboDetailList) {
				combXmlStr += 
				"		<ComboDetail>\n" +
				"			<tradeingNo>"+(comboDetail.get("order_sn")==null?"":comboDetail.get("order_sn"))+"</tradeingNo>\n" +
				"		</ComboDetail>\n";
			}
			combXmlStr += 
				"	</ComboDetailList>\n" +
				"</Combo>\n" +
				"</ComboList>\n" +
				"</Declaration>\n" +
				"</SubjectInfo>";
		return combXmlStr;
	}
	
	/**
	 * 装箱单报文
	 * @param opType
	 * @param sender
	 * @param packingList
	 * @return
	 */
	public String getPackXmlStr(String opType, Map<String, Object> packing){
		int num=(int)(Math.random()*9000)+1000;
		String messageId = "PACK_"+packing.get("custom_corp")+"_"+DateUtils.getDate("yyyyMMddHHmmss")+""+num;
		String packXmlStr = 
				"<?xml version='1.0' encoding='utf-8'?>\n" +
				"<SubjectInfo>\n" +
				"<Head>\n" +
				"<MessageId>"+messageId+"</MessageId>\n" +
				"	<MessageType>PACK</MessageType>\n" +
				"	<Sender>"+packing.get("create_org")+"</Sender>\n" +
				"	<Receiver>GZSW</Receiver>\n" +
				"	<opType>"+opType+"</opType>\n" +
				"</Head>\n" +
				"<Declaration>\n" +
				"<PackingList>\n" +
				"<Packing>\n" +
				"   <customCorp>"+(packing.get("custom_corp")==null?"":packing.get("custom_corp"))+"</customCorp>\n" +
				"   <packDate>"+(packing.get("encase_date")==null?"":packing.get("encase_date"))+"</packDate>\n" +
				"   <packWarehouse>"+(packing.get("warehouse_name")==null?"":packing.get("warehouse_name"))+"</packWarehouse>\n" +
				"   <destCountry>"+(packing.get("dest_country_id")==null?"":packing.get("dest_country_id"))+"</destCountry>\n" +
				"   <billCode>"+(packing.get("lading_no")==null?"":packing.get("lading_no"))+"</billCode>\n" +
				"   <customProxyer>"+(packing.get("custom_proxyer")==null?"":packing.get("custom_proxyer"))+"</customProxyer>\n" +
				"   <ciqCorp>"+(packing.get("ciq_corp")==null?"":packing.get("ciq_corp"))+"</ciqCorp>\n" +
				"	<billNo>"+(packing.get("encase_no")==null?"":packing.get("encase_no"))+"</billNo>\n" +
				"	<packingId>"+(packing.get("id")==null?"":packing.get("id"))+"</packingId>\n" +
				"	<createOrg>"+(packing.get("create_org")==null?"":packing.get("create_org"))+"</createOrg>\n" +
				"	<reason></reason>\n" +
				"   <PackingDetailList>\n";
			List<Map<String, Object>> packingDetailList = this.findForJdbc("select smt_group.group_sn,smt_encasement_detail.container_name,smt_encasement_detail.container_number from smt_encasement_detail,smt_group where smt_encasement_detail.group_id in (smt_group.id,smt_group.group_sn) and smt_encasement_detail.encase_id = ?", new Object[]{packing.get("id")});
			for (Map<String, Object> packingDetail : packingDetailList) {
				packXmlStr += 
				"	<PackingDetail>\n" +
			    "		<comboId>"+(packingDetail.get("group_sn")==null?"":packingDetail.get("group_sn"))+"</comboId>\n" +
				"	    <containerType>"+(packingDetail.get("container_name")==null?"":packingDetail.get("container_name"))+"</containerType>\n" +
				"	    <containerNo>"+(packingDetail.get("container_number")==null?"":packingDetail.get("container_number"))+"</containerNo>\n" +
				"	</PackingDetail>\n";
			}
			packXmlStr +=
				"	</PackingDetailList>\n" +
				"</Packing>\n" +
				"</PackingList>\n" +
				"</Declaration>\n" +
				"</SubjectInfo>";
		return packXmlStr;
	}
	
	/**
	 * 报关单报文
	 * @param opType
	 * @param custom
	 * @return
	 */
	public String getCustomXmlStr(String opType,  Map<String,Object> custom) throws Exception{
		int num=(int)(Math.random()*9000)+1000;
		String messageId = "CUSTOM_"+custom.get("create_org")+"_"+DateUtils.getDate("yyyyMMddHHmmss")+""+num;
		String customXmlStr = 
				"<?xml version=\"1.0\" encoding=\"utf-8\"?>\n" +
				"<SubjectInfo>\n" +
				"	<Head>\n" +
				"		<MessageId>"+messageId+"</MessageId>\n" +
				"		<MessageType>CUSTOM</MessageType>\n" +
				"		<Sender>"+custom.get("create_org")+"</Sender>\n" +
				"		<Receiver>GZSW</Receiver>\n" +
				"		<opType>"+opType+"</opType>\n" +
				//"       <sendWay>1</sendWay>\n" +  //自动发送必填【1,2】，1为报关普通发送，2为报关V3发送。暂默认1
				"	</Head>\n" +
				"	<Declaration>\n" +
				"		<CustomHead>\n" +
				"			<packingNo>"+(custom.get("encase_no")==null?"":custom.get("encase_no"))+"</packingNo>\n" +
				"           <landTransportMode>"+(custom.get("land_transport_code")==null?"":custom.get("land_transport_code"))+"</landTransportMode>\n" +
				"			<customName>"+asd.EncryStrHex(asd.GB2Code(custom.get("customName").toString()), "GZMARKET2017")+"</customName>\n" +
				"			<customPassword>"+asd.EncryStrHex(asd.GB2Code(custom.get("customPassword").toString()), "GZMARKET2017")+"</customPassword>\n" +
				"			<opType>"+(custom.get("business_type")==null?"":custom.get("business_type"))+"</opType>\n" +
				"			<decTin>"+(custom.get("dec_or_transfer")==null?"":custom.get("dec_or_transfer"))+"</decTin>\n" +
				"			<decMode>"+(custom.get("dec_model")==null?"":custom.get("dec_model"))+"</decMode>\n" +
				"			<checkSurety>"+(custom.get("guarantee_shall")==null?"":custom.get("guarantee_shall"))+"</checkSurety>\n" +
				"			<billType>"+(custom.get("list_type")==null?"":custom.get("list_type"))+"</billType>\n" +
				"			<seqNo>"+(custom.get("number")==null?"":custom.get("number"))+"</seqNo>\n" +
				"			<preEntryId>"+(custom.get("input_no")==null?"":custom.get("input_no"))+"</preEntryId>\n" +
				"			<entryId>"+(custom.get("dec_code")==null?"":custom.get("dec_code"))+"</entryId>\n" +
				"			<iEPort>"+(custom.get("export_port_code")==null?"":custom.get("export_port_code"))+"</iEPort>\n" +
				"			<contrNo>"+(custom.get("contract_no")==null?"":custom.get("contract_no"))+"</contrNo>\n" +
				"			<iEDate>"+(custom.get("export_date")==null?"":DateUtils.dateformat(custom.get("export_date").toString(), "yyyy-MM-dd"))+"</iEDate>\n" +
				"			<dDate>"+(custom.get("dec_date")==null?"":custom.get("dec_date"))+"</dDate>\n" +
				"			<tradeCo>"+(custom.get("trade_co")==null?"":custom.get("trade_co"))+"</tradeCo>\n" +
				"			<tradeCoName>"+(custom.get("consigner")==null?"":custom.get("consigner"))+"</tradeCoName>\n" +
				"			<coOwner>"+(custom.get("com_type")==null?"":custom.get("com_type"))+"</coOwner>\n" +
				"			<tradeCoscc></tradeCoscc>\n" +
				"			<ownerCode>"+(custom.get("owner_code")==null?"":custom.get("owner_code"))+"</ownerCode>\n" +
				"			<ownerName>"+(custom.get("sales_unit")==null?"":custom.get("sales_unit"))+"</ownerName>\n" +
				"			<ownerCodeScc>"+(custom.get("shipper_uniform_no")==null?"":custom.get("shipper_uniform_no"))+"</ownerCodeScc>\n" +
				"			<agentCode>"+(custom.get("agent_code")==null?"":custom.get("agent_code"))+"</agentCode>\n" +
				"			<agentName>"+(custom.get("apply_com")==null?"":custom.get("apply_com"))+"</agentName>\n" +
				"			<agentCodeScc></agentCodeScc>\n" +
				"			<copCodeScc></copCodeScc>\n" +
				"			<trafMode>"+(custom.get("trans_type")==null?"":custom.get("trans_type"))+"</trafMode>\n" +
				"			<trafName>"+(custom.get("transport")==null?"":custom.get("transport"))+"</trafName>\n" +
				"			<voyageNo>"+(custom.get("voyage_no")==null?"":custom.get("voyage_no"))+"</voyageNo>\n" +
				"			<billNo>"+(custom.get("lading_no")==null?"":custom.get("lading_no"))+"</billNo>\n" +
				"			<tradeMode>"+(custom.get("supervise_code")==null?"":custom.get("supervise_code"))+"</tradeMode>\n" +
				"			<cutMode>"+(custom.get("nature_shall_code")==null?"":custom.get("nature_shall_code"))+"</cutMode>\n" +
				"			<paymentMark>"+(custom.get("tax_unit")==null?"":custom.get("tax_unit"))+"</paymentMark>\n" +
				"			<licenseNo>"+(custom.get("license_key")==null?"":custom.get("license_key"))+"</licenseNo>\n" +
				"			<tradeAreaCode>"+(custom.get("trade_country_code")==null?"":custom.get("trade_country_code"))+"</tradeAreaCode>\n" +
				"			<tradeCountry>"+(custom.get("arrive_country_code")==null?"":custom.get("arrive_country_code"))+"</tradeCountry>\n" +
				"			<distinatePort>"+(custom.get("arrive_port_code")==null?"":custom.get("arrive_port_code"))+"</distinatePort>\n" +
				"			<districtCode>"+(custom.get("goods_source_code")==null?"":custom.get("goods_source_code"))+"</districtCode>\n" +
				"			<apprNo>"+(custom.get("approval_num")==null?"":custom.get("approval_num"))+"</apprNo>\n" +
				"			<promiseItems>000</promiseItems>\n" +
				"			<transMode>"+(custom.get("deal_way")==null?"":custom.get("deal_way"))+"</transMode>\n" +
				"			<feeMark>"+(custom.get("freight_item")==null?"":custom.get("freight_item"))+"</feeMark>\n" +
				"			<feeRate>"+(custom.get("freight")==null?"":custom.get("freight"))+"</feeRate>\n" +
				"			<feeCurr>"+(custom.get("freight_currency")==null?"":custom.get("freight_currency"))+"</feeCurr>\n" +
				"			<insurMark>"+(custom.get("premium_item")==null?"":custom.get("premium_item"))+"</insurMark>\n" +
				"			<insurRate>"+(custom.get("premium")==null?"":custom.get("premium"))+"</insurRate>\n" +
				"			<insurCurr>"+(custom.get("premium_currency")==null?"":custom.get("premium_currency"))+"</insurCurr>\n" +
				"			<otherMark>"+(custom.get("fee_item")==null?"":custom.get("fee_item"))+"</otherMark>\n" +
				"			<otherRate>"+(custom.get("fee")==null?"":custom.get("fee"))+"</otherRate>\n" +
				"			<otherCurr>"+(custom.get("fee_currency")==null?"":custom.get("fee_currency"))+"</otherCurr>\n" +
				"			<packNo>"+(custom.get("piece_num")==null?"":custom.get("piece_num"))+"</packNo>\n" +
				"			<wrapType>"+(custom.get("packing_type")==null?"":custom.get("packing_type"))+"</wrapType>\n" +
				"			<grossWt>"+(custom.get("gross_weight")==null?"":custom.get("gross_weight"))+"</grossWt>\n" +
				"			<netWt>"+(custom.get("net_weight")==null?"":custom.get("net_weight"))+"</netWt>\n" +
				"			<containerNum>"+(custom.get("container_num")==null?"":custom.get("container_num"))+"</containerNum>\n" +
				"			<certMark></certMark>\n" +
				"			<copId>"+(custom.get("cop_id")==null?"":custom.get("cop_id"))+"</copId>\n" +
				"			<customMaster>"+(custom.get("dec_custom_code")==null?"":custom.get("dec_custom_code"))+"</customMaster>\n" +
				"			<relativeId>"+(custom.get("relate_cus_declaration")==null?"":custom.get("relate_cus_declaration"))+"</relativeId>\n" +
				"			<relativeManualNo>"+(custom.get("relate_record")==null?"":custom.get("relate_record"))+"</relativeManualNo>\n" +
				"			<bondedNo>"+(custom.get("bonded_place")==null?"":custom.get("bonded_place"))+"</bondedNo>\n" +
				"			<customsField>"+(custom.get("storage_area_code")==null?"":custom.get("storage_area_code"))+"</customsField>\n" +
				"			<typistNo>"+(custom.get("operator")==null?"":custom.get("operator"))+"</typistNo>\n" +
				"			<bpNo>"+(custom.get("contact_way")==null?"":custom.get("contact_way"))+"</bpNo>\n" +
				"			<entryType>"+(custom.get("relate_dec_model")==null?"":custom.get("relate_dec_model"))+"</entryType>\n" +
				"			<noteS>"+(custom.get("remark")==null?"":custom.get("remark"))+"</noteS>\n" +
				"           <createOrg>"+custom.get("create_org")+"</createOrg>\n" +
				"		</CustomHead>\n";
				//集装箱信息
				String containerSql = "select * from smt_container where custom_id = ?";
				List<Map<String, Object>> containerList = this.findForJdbc(containerSql, new Object[]{custom.get("id")});
				if(containerList!=null && containerList.size()>0){
					customXmlStr += "		<ContainerList>\n";
					for (Map<String, Object> container : containerList) {
						customXmlStr += 
								"			<Container>\n" +
								"				<containerId>"+(container.get("container_id")==null?"":container.get("container_id"))+"</containerId>\n" +
								"				<containerMd>"+(container.get("container_md")==null?"":container.get("container_md"))+"</containerMd>\n" +
								"				<containerWt>"+(container.get("container_wt")==null?"":container.get("container_wt"))+"</containerWt>\n" +
								"			</Container>\n";
					}
					customXmlStr += "		</ContainerList>\n";
				}
				//随附单据信息
				String certSql = "select * from smt_custom_decl_certificate where custom_id = ?";
				List<Map<String, Object>> certList = this.findForJdbc(certSql, new Object[]{custom.get("id")});
				if(certList!=null && certList.size()>0){
					customXmlStr += "		<CustomDeclCertificateList>\n";
					for (Map<String, Object> cert : certList) {
						customXmlStr += 
								"			<CustomDeclCertificate>\n" +
								"				<docuCode>"+(cert.get("docu_code")==null?"":cert.get("docu_code"))+"</docuCode>\n" +
								"				<certCode>"+(cert.get("cert_code")==null?"":cert.get("cert_code"))+"</certCode>\n" +
								"			</CustomDeclCertificate>\n";
					}
					customXmlStr += "		</CustomDeclCertificateList>\n";
				}
				//商品信息
				String customGoodsSql = 
						"SELECT\n" +
						"   smt_goods.number,\n" +
						"   smt_order_detail.trade_detail_id_return,\n" +
						"	smt_goods.hs_code,\n" +
						"   smt_goods.extra_code,\n" +
						"	smt_goods.goods_name,\n" +
						"	smt_goods.spec,\n" +
						"   smt_encasemen.dest_country_id,\n" +
						"   smt_order_detail.goods_num,\n" +
						"   smt_goods.amount_unit_code,\n" +
						"   smt_order_detail.goods_price,\n" +
						"	smt_order_detail.currency,\n" +
						"	smt_order_detail.total_amount,\n" +
						"   smt_order_detail.legal_num,\n" +
						"   smt_goods.weight_unit_code, \n" +
						"   smt_order_detail.second_num,\n" +
						"   smt_goods.second_unit_code,\n" +
						"   smt_decration_cus.nature_shall\n" +
						"FROM\n" +
						"   smt_decration_cus,\n" +
						"	smt_encasemen,\n" +
						"	smt_encasement_detail,\n" +
						"	smt_group,\n" +
						"	smt_group_detail,\n" +
						"	smt_order,\n" +
						"	smt_order_detail,\n" +
						"	smt_goods\n" +
						"WHERE\n" +
						"    smt_decration_cus.id = ?\n" +
						"AND smt_decration_cus.encase_id = smt_encasemen.id\n" +
						"AND smt_encasemen.id = smt_encasement_detail.encase_id\n" +
						"AND smt_encasement_detail.group_id = smt_group.id\n" +
						"AND smt_group.id = smt_group_detail.group_id\n" +
						"AND smt_group_detail.order_id = smt_order.id\n" +
						"AND smt_order.id = smt_order_detail.order_id\n" +
						"AND smt_order_detail.goods_id in( smt_goods.id,smt_goods.cg_goods_code)";
				List<Map<String, Object>> customGoodsList = this.findForJdbc(customGoodsSql, new Object[]{custom.get("id")});
				if(customGoodsList!=null && customGoodsList.size()>0){
					customXmlStr += "		<CustomGoodsList>\n";
					for (int i = 0; i < customGoodsList.size(); i++) {
						Map<String, Object> customGoods = customGoodsList.get(i);
						customXmlStr += 
								"			<CustomGoods>\n" +
								"				<gNo>"+(i+1)+"</gNo>\n" +
								"				<tradingGoodsId>"+(customGoods.get("trade_detail_id_return")==null?"":customGoods.get("trade_detail_id_return"))+"</tradingGoodsId>\n" +
								"				<codeT>"+(customGoods.get("hs_code")==null?"":customGoods.get("hs_code"))+"</codeT>\n" +
								"				<codeS>"+(customGoods.get("extra_code")==null?"":customGoods.get("extra_code"))+"</codeS>\n" +
								"				<gName>"+(customGoods.get("goods_name")==null?"":customGoods.get("goods_name"))+"</gName>\n" +
								"				<gModel>"+(customGoods.get("spec")==null?"":customGoods.get("spec"))+"</gModel>\n" +
								"				<controlMa></controlMa>\n" +
								"				<destinationCountry>142</destinationCountry>\n" +
								"				<originCountry>"+(customGoods.get("dest_country_id")==null?"":customGoods.get("dest_country_id"))+"</originCountry>\n" +
								"				<qty1>"+(customGoods.get("goods_num")==null?"":customGoods.get("goods_num"))+"</qty1>\n" +
								"				<unit1>"+(customGoods.get("amount_unit_code")==null?"":customGoods.get("amount_unit_code"))+"</unit1>\n" +
								"				<declPrice>"+(customGoods.get("goods_price")==null?"":customGoods.get("goods_price"))+"</declPrice>\n" +
								"				<tradeCurr>502</tradeCurr>\n" +
								"				<declTotal>"+(customGoods.get("total_amount")==null?"":customGoods.get("total_amount"))+"</declTotal>\n" +
								"				<gQty>"+(customGoods.get("legal_num")==null?"":customGoods.get("legal_num"))+"</gQty>\n" +
								"				<gUnit>"+(customGoods.get("weight_unit_code")==null?"":customGoods.get("weight_unit_code"))+"</gUnit>\n" +
								"				<exgVersion></exgVersion>\n" +
								"				<exgNo></exgNo>\n" +
								"				<qtyy2>"+(customGoods.get("second_num")==null?"":customGoods.get("second_num"))+"</qtyy2>\n" +
								"				<unit2>"+(customGoods.get("second_unit_code")==null?"":customGoods.get("second_unit_code"))+"</unit2>\n" +
								"				<dutyName>1</dutyName>\n" +
								"			</CustomGoods>\n";
					}
					customXmlStr += "		</CustomGoodsList>\n";
				}
				//转关提前
				if(custom.get("dec_or_transfer").toString().equals("003")){
					customXmlStr += 
							"		<AheadOfTransit>\n" +
							"			<AheadOfTransitHead>\n" +
							"				<turnNo>"+(custom.get("turn_no")==null?"":custom.get("turn_no"))+"</turnNo>\n" +
							"				<trnType>0</trnType>\n" +
							"				<applCodeScc></applCodeScc>\n" +
							"				<nativeTrafMode>"+(custom.get("native_traf_mode")==null?"":custom.get("native_traf_mode"))+"</nativeTrafMode>\n" +
							"				<trafCustomsNo>"+(custom.get("traf_customs_no")==null?"":custom.get("traf_customs_no"))+"</trafCustomsNo>\n" +
							"				<nativeShipName>"+(custom.get("native_ship_name")==null?"":custom.get("native_ship_name"))+"</nativeShipName>\n" +
							"				<nativeVoyageNo>"+(custom.get("native_voyage_no")==null?"":custom.get("native_voyage_no"))+"</nativeVoyageNo>\n" +
							"				<extendField>"+(custom.get("extend_field")==null?"":custom.get("extend_field"))+"</extendField>\n" +
							"				<contractorCode>111111111</contractorCode>\n" +
							"				<contractorName></contractorName>\n" +
							"				<validTime>"+(custom.get("valid_time")==null?"":custom.get("valid_time"))+"</validTime>\n" +
							"			</AheadOfTransitHead>\n" +
							"			<AheadOfTransitBill>\n" +
							"				<recordNumber>"+(custom.get("record_number")==null?"":custom.get("record_number"))+"</recordNumber>\n" +
							"				<shipId>"+(custom.get("ship_id")==null?"":custom.get("ship_id"))+"</shipId>\n" +
							"				<shipNameEn>"+(custom.get("ship_name_en")==null?"":custom.get("ship_name_en"))+"</shipNameEn>\n" +
							"				<voyageNo>"+(custom.get("voyageno")==null?"":custom.get("voyageno"))+"</voyageNo>\n" +
							"				<billNo>"+(custom.get("bill_no")==null?"":custom.get("bill_no"))+"</billNo>\n" +
							"				<contaC>0</contaC>\n" +
							"				<iEDate>"+(custom.get("ie_date")==null?"":custom.get("ie_date"))+"</iEDate>\n" +
							"			</AheadOfTransitBill>\n" +
							"       </AheadOfTransit>";
				}
				customXmlStr += 
						"	</Declaration>\n" +
						"</SubjectInfo>";
				
		return customXmlStr;
	}
	
	/**
	 * 一次性录入报关单
	 * @param opType
	 * @param custom
	 * @return
	 */
	public String getAutoXmlStr(String opType,  Map<String,Object> custom, SmtMainInfoEntity smtMainInfo){
		int num=(int)(Math.random()*9000)+1000;
		String messageId = "AUTO_"+custom.get("create_org")+"_"+DateUtils.getDate("yyyyMMddHHmmss")+""+num;
		StringBuilder sb = new StringBuilder();
		sb.append("<?xml version=\"1.0\" encoding=\"utf-8\"?>\n");
		sb.append("<SubjectInfo>\n");
		sb.append("	<Head>\n");
		sb.append("		<MessageId>").append(messageId).append("</MessageId>\n");
		sb.append("		<MessageType>AUTO</MessageType>\n");
		sb.append("		<Sender>").append(custom.get("create_org")).append("</Sender>\n");
		sb.append("		<Receiver>GZSW</Receiver>\n");
		sb.append("		<opType>").append(opType).append("</opType>\n");
		sb.append("	</Head>\n");
		sb.append("	<Declaration>\n");
		sb.append("		<CustomHead>\n");
		sb.append("			<opType>").append((custom.get("business_type")==null?"":custom.get("business_type"))).append("</opType>\n");
		sb.append("			<decTin>").append((custom.get("dec_or_transfer")==null?"":custom.get("dec_or_transfer"))).append("</decTin>\n");
		sb.append("			<decMode>").append((custom.get("dec_model")==null?"":custom.get("dec_model"))).append("</decMode>\n");
		sb.append("			<checkSurety>").append((custom.get("guarantee_shall")==null?"":custom.get("guarantee_shall"))).append("</checkSurety>\n");
		sb.append("			<billType>").append((custom.get("list_type")==null?"":custom.get("list_type"))).append("</billType>\n");
		sb.append("			<seqNo/>\n");
		sb.append("			<preEntryId/>\n");
		sb.append("			<entryId/>\n");
		sb.append("			<iEPort>").append((custom.get("export_port_code")==null?"":custom.get("export_port_code"))).append("</iEPort>\n");
		sb.append("			<contrNo>").append((custom.get("contract_no")==null?"":custom.get("contract_no"))).append("</contrNo>\n");
		sb.append("			<iEDate>").append((custom.get("export_date")==null?"":DateUtils.dateformat(custom.get("export_date").toString(), "yyyy-MM-dd"))).append("</iEDate>\n");
		sb.append("			<dDate>").append((custom.get("dec_date")==null?"":custom.get("dec_date"))).append("</dDate>\n");
		sb.append("			<tradeCo>").append((custom.get("trade_co")==null?"":custom.get("trade_co"))).append("</tradeCo>\n");
		sb.append("			<tradeCoName>").append((custom.get("consigner")==null?"":custom.get("consigner"))).append("</tradeCoName>\n");
		sb.append("			<coOwner>").append((custom.get("com_type")==null?"":custom.get("com_type"))).append("</coOwner>\n");
		sb.append("			<tradeCoscc></tradeCoscc>\n");
		sb.append("			<ownerCode>").append((custom.get("owner_code")==null?"":custom.get("owner_code"))).append("</ownerCode>\n");
		sb.append("			<ownerName>").append((custom.get("sales_unit")==null?"":custom.get("sales_unit"))).append("</ownerName>\n");
		sb.append("			<ownerCodeScc>").append((custom.get("shipper_uniform_no")==null?"":custom.get("shipper_uniform_no"))).append("</ownerCodeScc>\n");
		sb.append("			<agentCode>").append((custom.get("agent_code")==null?"":custom.get("agent_code"))).append("</agentCode>\n");
		sb.append("			<agentName>").append((custom.get("apply_com")==null?"":custom.get("apply_com"))).append("</agentName>\n");
		sb.append("			<agentCodeScc></agentCodeScc>\n");
		sb.append("			<copCodeScc></copCodeScc>\n");
		sb.append("			<trafMode>").append((custom.get("trans_type")==null?"":custom.get("trans_type"))).append("</trafMode>\n");
		sb.append("			<trafName>").append((custom.get("transport")==null?"":custom.get("transport"))).append("</trafName>\n");
		sb.append("			<voyageNo>").append((custom.get("voyage_no")==null?"":custom.get("voyage_no"))).append("</voyageNo>\n");
		sb.append("			<billNo>").append((custom.get("lading_no")==null?"":custom.get("lading_no"))).append("</billNo>\n");
		sb.append("			<tradeMode>").append((custom.get("supervise_code")==null?"":custom.get("supervise_code"))).append("</tradeMode>\n");
		sb.append("			<cutMode>").append((custom.get("nature_shall_code")==null?"":custom.get("nature_shall_code"))).append("</cutMode>\n");
		sb.append("			<paymentMark>").append((custom.get("tax_unit")==null?"":custom.get("tax_unit"))).append("</paymentMark>\n");
		sb.append("			<licenseNo>").append((custom.get("license_key")==null?"":custom.get("license_key"))).append("</licenseNo>\n");
		sb.append("			<tradeAreaCode>").append((custom.get("trade_country_code")==null?"":custom.get("trade_country_code"))).append("</tradeAreaCode>\n");
		sb.append("			<tradeCountry>").append((custom.get("arrive_country_code")==null?"":custom.get("arrive_country_code"))).append("</tradeCountry>\n");
		sb.append("			<distinatePort>").append((custom.get("arrive_port_code")==null?"":custom.get("arrive_port_code"))).append("</distinatePort>\n");
		sb.append("			<districtCode>").append((custom.get("goods_source_code")==null?"":custom.get("goods_source_code"))).append("</districtCode>\n");
		sb.append("			<apprNo>").append((custom.get("approval_num")==null?"":custom.get("approval_num"))).append("</apprNo>\n");
		sb.append("			<promiseItems/>\n");
		sb.append("			<transMode>").append((custom.get("deal_way")==null?"":custom.get("deal_way"))).append("</transMode>\n");
		sb.append("			<feeMark>").append((custom.get("freight_item")==null?"":custom.get("freight_item"))).append("</feeMark>\n");
		sb.append("			<feeRate>").append((custom.get("freight")==null?"":custom.get("freight"))).append("</feeRate>\n");
		sb.append("			<feeCurr>").append((custom.get("freight_currency")==null?"":custom.get("freight_currency"))).append("</feeCurr>\n");
		sb.append("			<insurMark>").append((custom.get("premium_item")==null?"":custom.get("premium_item"))).append("</insurMark>\n");
		sb.append("			<insurRate>").append((custom.get("premium")==null?"":custom.get("premium"))).append("</insurRate>\n");
		sb.append("			<insurCurr>").append((custom.get("premium_currency")==null?"":custom.get("premium_currency"))).append("</insurCurr>\n");
		sb.append("			<otherMark>").append((custom.get("fee_item")==null?"":custom.get("fee_item"))).append("</otherMark>\n");
		sb.append("			<otherRate>").append((custom.get("fee")==null?"":custom.get("fee"))).append("</otherRate>\n");
		sb.append("			<otherCurr>").append((custom.get("fee_currency")==null?"":custom.get("fee_currency"))).append("</otherCurr>\n");
		sb.append("			<packNo>").append((custom.get("piece_num")==null?"":custom.get("piece_num"))).append("</packNo>\n");
		sb.append("			<wrapType>").append((custom.get("packing_type")==null?"":custom.get("packing_type"))).append("</wrapType>\n");
		sb.append("			<grossWt>").append((custom.get("gross_weight")==null?"":custom.get("gross_weight"))).append("</grossWt>\n");
		sb.append("			<netWt>").append((custom.get("net_weight")==null?"":custom.get("net_weight"))).append("</netWt>\n");
		sb.append("			<containerNum>").append((custom.get("container_num")==null?"":custom.get("container_num"))).append("</containerNum>\n");
		sb.append("			<certMark></certMark>\n");
		sb.append("			<copId>").append((custom.get("cop_id")==null?"":custom.get("cop_id"))).append("</copId>\n");
		sb.append("			<customMaster>").append((custom.get("dec_custom_code")==null?"":custom.get("dec_custom_code"))).append("</customMaster>\n");
		sb.append("			<relativeId>").append((custom.get("relate_cus_declaration")==null?"":custom.get("relate_cus_declaration"))).append("</relativeId>\n");
		sb.append("			<relativeManualNo>").append((custom.get("relate_record")==null?"":custom.get("relate_record"))).append("</relativeManualNo>\n");
		sb.append("			<bondedNo>").append((custom.get("bonded_place")==null?"":custom.get("bonded_place"))).append("</bondedNo>\n");
		sb.append("			<customsField>").append((custom.get("storage_area_code")==null?"":custom.get("storage_area_code"))).append("</customsField>\n");
		sb.append("			<typistNo>").append((custom.get("operator")==null?"":custom.get("operator"))).append("</typistNo>\n");
		sb.append("			<bpNo>").append((custom.get("contact_way")==null?"":custom.get("contact_way"))).append("</bpNo>\n");
		sb.append("			<entryType>").append((custom.get("relate_dec_model")==null?"":custom.get("relate_dec_model"))).append("</entryType>\n");
		sb.append("			<noteS>").append((custom.get("remark")==null?"":custom.get("remark"))).append("</noteS>\n");
		sb.append("         <createOrg>").append(custom.get("create_org")).append("</createOrg>\n");
		sb.append("         <ciqComp>").append(smtMainInfo.getQualityComCode()).append("</ciqComp>\n");
		sb.append("         <packComp>").append(smtMainInfo.getDeputyComCode()).append("</packComp>\n");
		sb.append("         <packWarehouse></packWarehouse>\n");
		sb.append("		</CustomHead>\n");
				//集装箱信息
				String containerSql = "select * from smt_container where custom_id = ?";
				List<Map<String, Object>> containerList = this.findForJdbc(containerSql, new Object[]{custom.get("id")});
				if(containerList!=null && containerList.size()>0){
					sb.append("		<ContainerList>\n");
					for (Map<String, Object> container : containerList) {
						sb.append("			<Container>\n");
						sb.append("				<containerId>").append((container.get("container_id")==null?"":container.get("container_id"))).append("</containerId>\n");
						sb.append("				<containerNo>").append(smtMainInfo.getContainerId()).append("</containerNo>\n");
						sb.append("				<containerMd>").append((container.get("container_md")==null?"":container.get("container_md"))).append("</containerMd>\n");
						sb.append("				<containerWt>").append((container.get("container_wt")==null?"":container.get("container_wt"))).append("</containerWt>\n");
					    sb.append("			</Container>\n");
					}
					sb.append("		</ContainerList>\n");
				}
				//随附单据信息
				String certSql = "select * from smt_custom_decl_certificate where custom_id = ?";
				List<Map<String, Object>> certList = this.findForJdbc(certSql, new Object[]{custom.get("id")});
				if(certList!=null && certList.size()>0){
					sb.append("		<CustomDeclCertificateList>\n");
					for (Map<String, Object> cert : certList) {
						sb.append("			<CustomDeclCertificate>\n");
						sb.append("				<docuCode>").append((cert.get("docu_code")==null?"":cert.get("docu_code"))).append("</docuCode>\n");
						sb.append("				<certCode>").append((cert.get("cert_code")==null?"":cert.get("cert_code"))).append("</certCode>\n");
						sb.append("			</CustomDeclCertificate>\n");
					}
					sb.append("		</CustomDeclCertificateList>\n");
				}
				//商品信息
				String customGoodsSql = 
						"SELECT\n" +
						"   smt_goods.cg_goods_code,\n" +
						"   smt_order_detail.trade_detail_id_return,\n" +
						"	smt_goods.hs_code,\n" +
						"   smt_goods.extra_code,\n" +
						"	smt_goods.goods_name,\n" +
						"	smt_goods.spec,\n" +
						"   smt_encasemen.dest_country_id,\n" +
						"   smt_order_detail.goods_num,\n" +
						"   smt_goods.amount_unit_code,\n" +
						"   smt_order_detail.goods_price,\n" +
						"	smt_order_detail.currency,\n" +
						"	smt_order_detail.total_amount,\n" +
						"   smt_order_detail.legal_num,\n" +
						"   smt_goods.weight_unit_code, \n" +
						"   smt_order_detail.second_num,\n" +
						"   smt_goods.second_unit_code,\n" +
						"   smt_decration_cus.nature_shall,\n" +
						"   smt_buyer.cert_num,\n" +
						"   smt_company.reg_num\n" +
						"FROM\n" +
						"   smt_decration_cus,\n" +
						"	smt_encasemen,\n" +
						"	smt_encasement_detail,\n" +
						"	smt_group,\n" +
						"	smt_group_detail,\n" +
						"	smt_order,\n" +
						"	smt_order_detail,\n" +
						"	smt_goods,\n" +
						"	smt_buyer,\n" +
						"	smt_company\n" +
						"WHERE\n" +
						"    smt_decration_cus.id = ?\n" +
						"AND smt_decration_cus.encase_id = smt_encasemen.id\n" +
						"AND smt_encasemen.id = smt_encasement_detail.encase_id\n" +
						"AND smt_encasement_detail.group_id = smt_group.id\n" +
						"AND smt_group.id = smt_group_detail.group_id\n" +
						"AND smt_group_detail.order_id = smt_order.id\n" +
						"AND smt_order.id = smt_order_detail.order_id\n" +
						"AND smt_order_detail.goods_id = smt_goods.id\n" +
						"AND smt_order.buyer_id = smt_buyer.id\n" +
						"AND smt_order.supplier_id = smt_company.create_by";
				System.out.println(customGoodsSql);
				List<Map<String, Object>> customGoodsList = this.findForJdbc(customGoodsSql, new Object[]{custom.get("id")});
				if(customGoodsList!=null && customGoodsList.size()>0){
					sb.append("		<CustomGoodsList>\n");
					for (int i = 0; i < customGoodsList.size(); i++) {
						Map<String, Object> customGoods = customGoodsList.get(i);
								sb.append("			<CustomGoods>\n");
								sb.append("				<gNo>").append((i+1)).append("</gNo>\n");
								sb.append("				<codeT>").append((customGoods.get("hs_code")==null?"":customGoods.get("hs_code"))).append("</codeT>\n");
								sb.append("				<codeS>").append((customGoods.get("extra_code")==null?"":customGoods.get("extra_code"))).append("</codeS>\n");
								sb.append("				<gName>").append((customGoods.get("goods_name")==null?"":customGoods.get("goods_name"))).append("</gName>\n");
								sb.append("				<gModel>").append((customGoods.get("spec")==null?"":customGoods.get("spec"))).append("</gModel>\n");
								sb.append("				<controlMa></controlMa>\n");
								sb.append("				<destinationCountry>142</destinationCountry>\n");
								sb.append("				<originCountry>").append((customGoods.get("dest_country_id")==null?"":customGoods.get("dest_country_id"))).append("</originCountry>\n");
								sb.append("				<qty1>").append((customGoods.get("goods_num")==null?"":customGoods.get("goods_num"))).append("</qty1>\n");
								sb.append("				<unit1>").append((customGoods.get("amount_unit_code")==null?"":customGoods.get("amount_unit_code"))).append("</unit1>\n");
								sb.append("				<declPrice>").append((customGoods.get("goods_price")==null?"":customGoods.get("goods_price"))).append("</declPrice>\n");
								sb.append("				<tradeCurr>502</tradeCurr>\n");
								sb.append("				<declTotal>").append((customGoods.get("total_amount")==null?"":customGoods.get("total_amount"))).append("</declTotal>\n");
								sb.append("				<gQty>").append((customGoods.get("legal_num")==null?"":customGoods.get("legal_num"))).append("</gQty>\n");
								sb.append("				<gUnit>").append((customGoods.get("weight_unit_code")==null?"":customGoods.get("weight_unit_code"))).append("</gUnit>\n");
								sb.append("				<exgVersion></exgVersion>\n");
								sb.append("				<exgNo></exgNo>\n");
								sb.append("				<qtyy2>").append((customGoods.get("second_num")==null?"":customGoods.get("second_num"))).append("</qtyy2>\n");
								sb.append("				<unit2>").append((customGoods.get("second_unit_code")==null?"":customGoods.get("second_unit_code"))).append("</unit2>\n");
								sb.append("				<dutyName>1</dutyName>\n");
								sb.append("				<goodsCode>").append((customGoods.get("cg_goods_code")==null?"":customGoods.get("cg_goods_code"))).append("</goodsCode>\n");
								sb.append("				<solderCode>").append((customGoods.get("reg_num")==null?"":customGoods.get("reg_num"))).append("</solderCode>\n");
								sb.append("				<buyer>").append((customGoods.get("cert_num")==null?"":customGoods.get("cert_num"))).append("</buyer>\n");
								sb.append("			</CustomGoods>\n");
					}
					sb.append("		</CustomGoodsList>\n");
				}
				//转关提前
				if(custom.get("dec_or_transfer").toString().equals("003")){
					sb.append("		<AheadOfTransit>\n");
					sb.append("			<AheadOfTransitHead>\n");
					sb.append("				<turnNo>").append((custom.get("turn_no")==null?"":custom.get("turn_no"))+"</turnNo>\n");
					sb.append("				<trnType>0</trnType>\n");
					sb.append("				<applCodeScc></applCodeScc>\n");
					sb.append("				<nativeTrafMode>").append((custom.get("native_traf_mode")==null?"":custom.get("native_traf_mode"))).append("</nativeTrafMode>\n");
					sb.append("				<trafCustomsNo>").append((custom.get("traf_customs_no")==null?"":custom.get("traf_customs_no"))).append("</trafCustomsNo>\n");
					sb.append("				<nativeShipName>").append((custom.get("native_ship_name")==null?"":custom.get("native_ship_name"))).append("</nativeShipName>\n");
					sb.append("				<nativeVoyageNo>").append((custom.get("native_voyage_no")==null?"":custom.get("native_voyage_no"))).append("</nativeVoyageNo>\n");
					sb.append("				<extendField>").append((custom.get("extend_field")==null?"":custom.get("extend_field"))).append("</extendField>\n");
					sb.append("				<contractorCode>111111111</contractorCode>\n");
					sb.append("				<contractorName></contractorName>\n");
					sb.append("				<validTime>").append((custom.get("valid_time")==null?"":custom.get("valid_time"))).append("</validTime>\n");
					sb.append("			</AheadOfTransitHead>\n");
					sb.append("			<AheadOfTransitBill>\n");
					sb.append("				<recordNumber>").append((custom.get("record_number")==null?"":custom.get("record_number"))).append("</recordNumber>\n");
					sb.append("				<shipId>").append((custom.get("ship_id")==null?"":custom.get("ship_id"))).append("</shipId>\n");
					sb.append("				<shipNameEn>").append((custom.get("ship_name_en")==null?"":custom.get("ship_name_en"))).append("</shipNameEn>\n");
					sb.append("				<voyageNo>").append((custom.get("voyageno")==null?"":custom.get("voyageno"))).append("</voyageNo>\n");
					sb.append("				<billNo>").append((custom.get("bill_no")==null?"":custom.get("bill_no"))).append("</billNo>\n");
					sb.append("				<contaC>0</contaC>\n");
					sb.append("				<iEDate>").append((custom.get("ie_date")==null?"":custom.get("ie_date"))).append("</iEDate>\n");
					sb.append("			</AheadOfTransitBill>\n");
					sb.append("       </AheadOfTransit>");
				}
		sb.append("	</Declaration>\n");
		sb.append("</SubjectInfo>");
		return sb.toString();
	}
	
	/**
	 * 报检单报文
	 * @param opType
	 * @param decl
	 * @return
	 */
	private String getDeclXmlStr(String opType, Map<String,Object> decl) throws Exception{
		int num=(int)(Math.random()*9000)+1000;
		String messageId = "DECL_"+decl.get("create_org")+"_"+DateUtils.getDate("yyyyMMddHHmmss")+""+num;
		String declXmlStr = 
				"<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
				"<SubjectInfo>\n" +
				"<Head>\n" +
				"	<MessageId>"+messageId+"</MessageId>\n" +
				"	<MessageType>DECL</MessageType>\n" +
				"   <Sender>"+decl.get("create_org")+"</Sender>\n" +
				"   <Receiver>GZSW</Receiver>\n" +
				"	<opType>"+opType+"</opType>\n" +
				//"   <sendWay>1</sendWay>\n" +  //自动发送必填【1】默认1
				"</Head>\n" +
				"<Declaration>\n" +
				"<InspectHead>\n" +
				"	<packingNo>"+(decl.get("packing_no")==null?"":decl.get("packing_no"))+"</packingNo>\n" +
				"	<customName>"+asd.EncryStrHex(asd.GB2Code(decl.get("customName").toString()), "GZMARKET2017")+"</customName>\n" +
				"	<customPassword>"+asd.EncryStrHex(asd.GB2Code(decl.get("customPassword").toString()), "GZMARKET2017")+"</customPassword>\n" +
				"	<relaid></relaid>\n" +
				"	<ciqBCode>"+(decl.get("ciq_bcode")==null?"":decl.get("ciq_bcode"))+"</ciqBCode>\n" +
				"	<declBCode>"+(decl.get("decl_bcode")==null?"":decl.get("decl_bcode"))+"</declBCode>\n" +
				"	<portCiqBCode>"+(decl.get("port_ciq_bcode")==null?"":decl.get("port_ciq_bcode"))+"</portCiqBCode>\n" +
				"	<stationBCode>1</stationBCode>\n" +
				"	<companyCode>"+(decl.get("company_code")==null?"":decl.get("company_code"))+"</companyCode>\n" +
				"	<invoiceNO>"+(decl.get("invoice_no")==null?"":decl.get("invoice_no"))+"</invoiceNO>\n" +
				"	<bargainNO>"+(decl.get("bargain_no")==null?"":decl.get("bargain_no"))+"</bargainNO>\n" +
				"	<payerCode>"+(decl.get("payer_code")==null?"":decl.get("payer_code"))+"</payerCode>\n" +
				"	<goodsName>"+(decl.get("goods_name")==null?"":decl.get("goods_name"))+"</goodsName>\n" +
				"	<goodType>"+(decl.get("good_type")==null?"":decl.get("good_type"))+"</goodType>\n" +
				"	<shipperCode>"+(decl.get("shipper_code")==null?"":decl.get("shipper_code"))+"</shipperCode>\n" +
				"	<portStationBCode>"+(decl.get("test_tube_code")==null?"":decl.get("test_tube_code"))+"</portStationBCode>\n" +
				"	<operatorCode>"+(decl.get("operator_code")==null?"":decl.get("operator_code"))+"</operatorCode>\n" +
				"	<storageCompany>"+(decl.get("storage_company")==null?"":decl.get("storage_company"))+"</storageCompany>\n" +
				"	<consignee>"+(decl.get("consignee")==null?"":decl.get("consignee"))+"</consignee>\n" +
				"	<consigneeAdr>"+(decl.get("consignee_adr")==null?"":decl.get("consignee_adr"))+"</consigneeAdr>\n" +
				"	<procurePlace>"+(decl.get("procure_place")==null?"":decl.get("procure_place"))+"</procurePlace>\n" +
				"	<loadPlace>"+(decl.get("load_place")==null?"":decl.get("load_place"))+"</loadPlace>\n" +
				"	<portLoad>"+(decl.get("port_load_code")==null?"":decl.get("port_load_code"))+"</portLoad>\n" +
				"	<portDis>"+(decl.get("port_dis_code")==null?"":decl.get("port_dis_code"))+"</portDis>\n" +
				"	<tradeType>"+(decl.get("trade_type_code")==null?"":decl.get("trade_type_code"))+"</tradeType>\n" +
				"	<packType>"+(decl.get("pack_type_code")==null?"":decl.get("pack_type_code"))+"</packType>\n" +
				"	<conOper>"+(decl.get("con_oper")==null?"":decl.get("con_oper"))+"</conOper>\n" +
				"	<operType>"+(decl.get("oper_type")==null?"":decl.get("oper_type"))+"</operType>\n" +
				"	<vesselCN>"+(decl.get("trans_name")==null?"":decl.get("trans_name"))+"</vesselCN>\n" +
				"	<voyage>"+(decl.get("voyage")==null?"":decl.get("voyage"))+"</voyage>\n" +
				"	<maniNO>"+(decl.get("mani_no")==null?"":decl.get("mani_no"))+"</maniNO>\n" +
				"	<fCode>"+(decl.get("currency")==null?"":decl.get("currency"))+"</fCode>\n" +
				"	<cousType>FOB</cousType>\n" +
				"	<payType>T/T</payType>\n" +
				"	<planOutDate>"+(decl.get("plan_out_date")==null?"":decl.get("plan_out_date"))+"</planOutDate>\n" +
				"	<payCondition>"+(decl.get("pay_condition")==null?"":decl.get("pay_condition"))+"</payCondition>\n" +
				"	<bentryFlags>"+(decl.get("bentry_flags")==null?"":decl.get("bentry_flags"))+"</bentryFlags>\n" +
				"	<workMode>"+(decl.get("work_mode")==null?"":decl.get("work_mode"))+"</workMode>\n" +
				"	<blno>"+(decl.get("blno")==null?"":decl.get("blno"))+"</blno>\n" +
				"	<vPrepare>"+(decl.get("prepare")==null?"":decl.get("prepare"))+"</vPrepare>\n" +
				"	<vPrepareName>"+(decl.get("prepare_name")==null?"":decl.get("prepare_name"))+"</vPrepareName>\n" +
				"	<platFormCode>"+(decl.get("plat_form_code")==null?"":decl.get("plat_form_code"))+"</platFormCode>\n" +
				"	<remark>"+(decl.get("remark")==null?"":decl.get("remark"))+"</remark>\n" +
				"   <createOrg>"+decl.get("create_org")+"</createOrg>\n" +
				"   <transportType>"+decl.get("trans_type")+"</transportType>\n" +
				"</InspectHead>\n" +
				"<InspectGoodsList>\n";
		String sql = 
				"SELECT\n" +
				"  smt_goods.hs_code,\n" +
				"  smt_goods.extra_code,\n" +		
				"  smt_goods.goods_name,\n" +
				"  smt_goods.goods_en_name,\n" +
				"  smt_goods.spec,\n" +
				"  smt_order.supplier,\n" +
				"  smt_order_detail.goods_price,\n" +
				"  smt_order_detail.goods_num,\n" +
				"  smt_order_detail.goods_unit,\n" +
				"  smt_order_detail.total_amount,\n" +
				"  smt_order_detail.trade_detail_id_return\n" +
				"FROM\n" +
				"	smt_encasemen,\n" +
				"	smt_encasement_detail,\n" +
				"	smt_group,\n" +
				"	smt_group_detail,\n" +
				"	smt_order,\n" +
				"	smt_order_detail,\n" +
				"	smt_goods\n" +
				"WHERE\n" +
				"	smt_encasemen.id = ?\n" +
				"AND smt_encasemen.id = smt_encasement_detail.encase_id\n" +
				"AND smt_encasement_detail.group_id = smt_group.id\n" +
				"AND smt_group.id = smt_group_detail.group_id\n" +
				"AND smt_group_detail.order_id = smt_order.id\n" +
				"AND smt_order.id = smt_order_detail.order_id\n" +
				"AND smt_order_detail.goods_id = smt_goods.id";	
		List<Map<String, Object>> inspectGoodsList = this.findForJdbc(sql, new Object[]{decl.get("packing_id")});
		for (Map<String, Object> goods : inspectGoodsList) {
			declXmlStr += 
					"	<InspectGoods>\n" +
					"		<tradingGoodsId>"+(goods.get("trade_detail_id_return")==null?"":goods.get("trade_detail_id_return"))+"</tradingGoodsId>\n" +
					"		<goodsName>"+(goods.get("goods_name")==null?"":goods.get("goods_name"))+"</goodsName>\n" +
					"		<cargoNameCN>"+(goods.get("goods_en_name")==null?"":goods.get("goods_en_name"))+"</cargoNameCN>\n" +
					"		<hsCode>"+(goods.get("hs_code")==null?"":goods.get("hs_code").toString()+goods.get("extra_code").toString())+"</hsCode> \n" +
					"		<spec>"+(goods.get("spec")==null?"":goods.get("spec"))+"</spec>\n" +
					"		<goodsMaterial></goodsMaterial>\n" +
					"		<produceComp></produceComp>\n" +
					"		<markNo></markNo> \n" +
					"		<assemCountry>990001</assemCountry>\n" +
					"		<packType>111</packType>\n" +
					"		<wsMarket>11</wsMarket>\n" +
					"		<individual>"+(goods.get("supplier")==null?"":goods.get("supplier"))+"</individual>\n" +
					"		<upric>"+(goods.get("goods_price")==null?"":goods.get("goods_price"))+"</upric>\n" +
					"		<qty>"+(goods.get("goods_num")==null?"":goods.get("goods_num"))+"</qty>\n" +
					"		<qtyUnit>"+(goods.get("goods_unit")==null?"":goods.get("goods_unit"))+"</qtyUnit>\n" +
					"		<qtyDesc></qtyDesc>\n" +
					"		<qtp></qtp>\n" +
					"		<qtpUnit></qtpUnit>\n" +
					"		<qtpDesc></qtpDesc>\n" +
					"		<kgs>3.0</kgs>\n" +
					"		<net>2.0</net>\n" +
					"		<fcy>"+(goods.get("total_amount")==null?"":goods.get("total_amount"))+"</fcy>\n" +
					"		<cousType>FOB</cousType>\n" +
					"		<packingComp></packingComp>\n" +
					"		<buyFromCity></buyFromCity>\n" +
					"		<remark></remark>\n" +
					"	</InspectGoods>\n";
		}
		declXmlStr += "</InspectGoodsList>\n";
		declXmlStr += 
				"</Declaration>\n" +
				"</SubjectInfo>";
		return declXmlStr;
	}
	
	/**
	 * 委托代理出口协议报文
	 * @param opType
	 * @param client_code
	 * @return
	 */
	public String getContXmlStr(String opType,  String entrustCorp){
		int num=(int)(Math.random()*9000)+1000;
		String messageId = "CONT_"+entrustCorp+"_"+DateUtils.getDate("yyyyMMddHHmmss")+""+num;
		String contXmlStr =
				"<?xml version='1.0' encoding='utf-8'?>\n" +
				"<SubjectInfo>\n" +
				"<Head>\n" +
				"<MessageId>"+messageId+"</MessageId>\n" +
				"		<MessageType>CONT</MessageType>\n" +
				"		<Sender>"+entrustCorp+"</Sender>\n" +
				"		<Receiver>GZSW</Receiver>\n" +
				"		<opType>"+opType+"</opType>\n" +
				"</Head>\n" +
				"<Declaration>\n" +
				"<EntrustAgentContract>\n" +
				"    <entrustCorp>"+entrustCorp+"</entrustCorp>\n" +
				"</EntrustAgentContract>\n" +
				"</Declaration>\n" +
				"</SubjectInfo>";
		return contXmlStr;
	}
	
	/**
	 * 代理出口货物证明报文
	 * @param opType
	 * @param entrustCorp
	 * @return
	 */
	public String getGoodsCertXmlStr(String opType,  String entrustCorp){
		int num=(int)(Math.random()*9000)+1000;
		String messageId = "GOODSCERT_"+entrustCorp+"_"+DateUtils.getDate("yyyyMMddHHmmss")+""+num;
		String contXmlStr =
				"<?xml version='1.0' encoding='utf-8'?>\n" +
				"<SubjectInfo>\n" +
				"<Head>\n" +
				"<MessageId>"+messageId+"</MessageId>\n" +
				"		<MessageType>GOODSCERT</MessageType>\n" +
				"		<Sender>"+entrustCorp+"</Sender>\n" +
				"		<Receiver>GZSW</Receiver>\n" +
				"		<opType>"+opType+"</opType>\n" +
				"</Head>\n" +
				"<Declaration>\n" +
				"<DsqAgentGoodsCert>\n" +
				"    <entrustCorp>"+entrustCorp+"</entrustCorp>\n" +
				"</DsqAgentGoodsCert>\n" +
				"</Declaration>\n" +
				"</SubjectInfo>";
		return contXmlStr;
	}
	
	/**
	 * 免税申报单报文
	 * @param opType
	 * @param entrustCorp
	 * @return
	 */
	public String getFreeXmlStr( String opType,String senderName, String applyCode){
		int num=(int)(Math.random()*9000)+1000;
		String messageId = "FREE_"+senderName+"_"+DateUtils.getDate("yyyyMMddHHmmss")+""+num;
		String contXmlStr =
				"<?xml version='1.0' encoding='utf-8'?>\n" +
				"<SubjectInfo>\n" +
				"<Head>\n" +
				"<MessageId>"+messageId+"</MessageId>\n" +
				"		<MessageType>FREE</MessageType>\n" +
				"		<Sender>"+senderName+"</Sender>\n" +
				"		<Receiver>GZSW</Receiver>\n" +
				"		<opType>"+opType+"</opType>\n" +
				"</Head>\n" +
				"<Declaration>\n" +
				"<DsqFreeTaxApply>\n" +
				"    <applyCode>"+applyCode+"</applyCode>\n" +
				"</DsqFreeTaxApply>\n" +
				"</Declaration>\n" +
				"</SubjectInfo>";
		return contXmlStr;
	}
	
	/**
	 * 电子随附单据报文
	 * @param smtDecrationCus
	 * @param smtCert
	 * @return
	 */
	private String getCertXmlStr(SmtDecrationCusEntity smtDecrationCus, SmtCertEntity smtCert){
		int num=(int)(Math.random()*9000)+1000;
		String messageId = "CERT_"+smtDecrationCus.getCreateBy()+"_"+DateUtils.getDate("yyyyMMddHHmmss")+""+num;
		StringBuilder sb = new StringBuilder();
		sb.append("<?xml version='1.0' encoding='utf-8'?>\n");
		sb.append("<SubjectInfo>\n");
		sb.append("<Head>\n");
		sb.append("<MessageId>").append(messageId).append("</MessageId>\n");
		sb.append("	<MessageType>CERT</MessageType>\n");
		sb.append("	<Sender>").append(smtDecrationCus.getCreateBy()).append("</Sender>\n");
		sb.append("	<Receiver>GZSW</Receiver>\n");
		sb.append("	<opType>A</opType>\n");
		sb.append("</Head>\n");
		sb.append("<Declaration>\n");
		sb.append("<CertificateList>\n");
	    if(StringUtils.isNotEmpty(smtCert.getInvoiceName()) && StringUtils.isNotEmpty(smtCert.getInvoiceFile())){
	    	String invoiceFile = HttpUtil.PDFToBase64(new File(ResourceUtil.getPorjectPath()+smtCert.getInvoiceFile()));
	    	sb.append("<Certificate>\n");
		    sb.append(" <copId>").append(smtDecrationCus.getCopId()).append("</copId>\n");
		    sb.append("	<fileType>00000001</fileType>\n");
		    sb.append("	<fileName>").append(smtCert.getInvoiceName()).append("</fileName>\n");
		    sb.append("	<edocNo>").append(smtCert.getInvoiceNo()).append("</edocNo>\n");
		    sb.append(" <files>"+invoiceFile+"</files>\n");
		    sb.append("</Certificate>\n");
	    }
	    if(StringUtils.isNotEmpty(smtCert.getPackingName()) && StringUtils.isNotEmpty(smtCert.getPackingFile())){
	    	String packingFile = HttpUtil.PDFToBase64(new File(ResourceUtil.getPorjectPath()+smtCert.getPackingFile()));
	    	sb.append("<Certificate>\n");
		    sb.append(" <copId>").append(smtDecrationCus.getCopId()).append("</copId>\n");
		    sb.append("	<fileType>00000002</fileType>\n");
		    sb.append("	<fileName>").append(smtCert.getPackingName()).append("</fileName>\n");
		    sb.append("	<edocNo>").append(smtCert.getPackingNo()).append("</edocNo>\n");
		    sb.append(" <files>"+packingFile+"</files>\n");
		    sb.append("</Certificate>\n");
	    }
	    if(StringUtils.isNotEmpty(smtCert.getLadingName()) && StringUtils.isNotEmpty(smtCert.getLadingFile())){
	    	String ladingFile = HttpUtil.PDFToBase64(new File(ResourceUtil.getPorjectPath()+smtCert.getLadingFile()));
	    	sb.append("<Certificate>\n");
		    sb.append(" <copId>").append(smtDecrationCus.getCopId()).append("</copId>\n");
		    sb.append("	<fileType>00000003</fileType>\n");
		    sb.append("	<fileName>").append(smtCert.getLadingName()).append("</fileName>\n");
		    sb.append("	<edocNo>").append(smtCert.getLadingNo()).append("</edocNo>\n");
		    sb.append(" <files>"+ladingFile+"</files>\n");
		    sb.append("</Certificate>\n");
	    }
	    if(StringUtils.isNotEmpty(smtCert.getContractName()) && StringUtils.isNotEmpty(smtCert.getContractFile())){
	    	String contractFile = HttpUtil.PDFToBase64(new File(ResourceUtil.getPorjectPath()+smtCert.getContractFile()));
	    	sb.append("<Certificate>\n");
		    sb.append(" <copId>").append(smtDecrationCus.getCopId()).append("</copId>\n");
		    sb.append("	<fileType>00000004</fileType>\n");
		    sb.append("	<fileName>").append(smtCert.getContractName()).append("</fileName>\n");
		    sb.append("	<edocNo>").append(smtCert.getContractNo()).append("</edocNo>\n");
		    sb.append(" <files>"+contractFile+"</files>\n");
		    sb.append("</Certificate>\n");
	    }
	    if(StringUtils.isNotEmpty(smtCert.getPaperCustomName()) && StringUtils.isNotEmpty(smtCert.getPaperCustomFile())){
	    	String paperCustomFile = HttpUtil.PDFToBase64(new File(ResourceUtil.getPorjectPath()+smtCert.getPaperCustomFile()));
	    	sb.append("<Certificate>\n");
		    sb.append(" <copId>").append(smtDecrationCus.getCopId()).append("</copId>\n");
		    sb.append("	<fileType>00000008</fileType>\n");
		    sb.append("	<fileName>").append(smtCert.getPaperCustomName()).append("</fileName>\n");
		    sb.append("	<edocNo>").append(smtCert.getPaperCustomNo()).append("</edocNo>\n");
		    sb.append(" <files>"+paperCustomFile+"</files>\n");
		    sb.append("</Certificate>\n");
	    }
	    if(StringUtils.isNotEmpty(smtCert.getElectronicAgentNo())){
	    	sb.append("<Certificate>\n");
		    sb.append(" <copId>").append(smtDecrationCus.getCopId()).append("</copId>\n");
		    sb.append("	<fileType>10000001</fileType>\n");
		    sb.append("	<fileName></fileName>\n");
		    sb.append("	<edocNo>").append(smtCert.getElectronicAgentNo()).append("</edocNo>\n");
		    sb.append(" <files></files>\n");
		    sb.append("</Certificate>\n");
	    }
	    if(StringUtils.isNotEmpty(smtCert.getFreetaxProve())){
	    	sb.append("<Certificate>\n");
		    sb.append(" <copId>").append(smtDecrationCus.getCopId()).append("</copId>\n");
		    sb.append("	<fileType>10000002</fileType>\n");
		    sb.append("	<fileName></fileName>\n");
		    sb.append("	<edocNo>").append(smtCert.getPaperCustomName()).append("</edocNo>\n");
		    sb.append(" <files></files>\n");
		    sb.append("</Certificate>\n");
	    }
	    if(StringUtils.isNotEmpty(smtCert.getFreetaxDelayProve())){
	    	String paperCustomFile = HttpUtil.PDFToBase64(new File(smtCert.getPaperCustomFile()));
	    	sb.append("<Certificate>\n");
		    sb.append(" <copId>").append(smtDecrationCus.getCopId()).append("</copId>\n");
		    sb.append("	<fileType>10000003</fileType>\n");
		    sb.append("	<fileName></fileName>\n");
		    sb.append("	<edocNo>").append(smtCert.getFreetaxDelayProve()).append("</edocNo>\n");
		    sb.append(" <files></files>\n");
		    sb.append("</Certificate>\n");
	    }
	    sb.append("</CertificateList>\n");
		sb.append("</Declaration>\n");
		sb.append("</SubjectInfo>");
		return sb.toString();
	}
	public static String sendGet(String url) {
		CloseableHttpClient httpClient = HttpClients.createDefault();
		HttpClientContext context = new HttpClientContext();
		CloseableHttpResponse response = null;
		String content = null;
		try {
			HttpGet get = new HttpGet(url);
			
			Cookie cookie = new Cookie("key", null);
			cookie.setMaxAge(0);
			get.removeHeaders("Cookies");
			get.removeHeaders("cookies");
			get.removeHeaders("cookie");
			get.removeHeaders("Cookie");
			response = httpClient.execute(get, context);
			HttpEntity entity = response.getEntity();
			content = EntityUtils.toString(entity);
			// System.out.println(TAG + "GET:" + content);
			EntityUtils.consume(entity);
			return content;
		} catch (Exception e) {
			e.printStackTrace();
			if (response != null) {
				try {
					response.close();
				} catch (IOException e1) {
					e1.printStackTrace();
				}
			}
		}
		return content;
	}

	public static String sendPost(String url, List<NameValuePair> nvps) {
		CloseableHttpClient httpClient = HttpClients.createDefault();
		HttpClientContext context = new HttpClientContext();
		CloseableHttpResponse response = null;
		String content = null;
		try {
			// 　HttpClient中的post请求包装类
			HttpPost post = new HttpPost(url);
			// nvps是包装请求参数的list
			if (nvps != null) {
				post.setEntity(new UrlEncodedFormEntity(nvps, "UTF-8"));
			}
			// 执行请求用execute方法，content用来帮我们附带上额外信息
			response = httpClient.execute(post, context);
			// 得到相应实体、包括响应头以及相应内容
			HttpEntity entity = response.getEntity();
			// 得到response的内容
			content = EntityUtils.toString(entity);
			// System.out.println(TAG + "POST:" + content);
			// 　关闭输入流
			EntityUtils.consume(entity);
			return content;
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			if (response != null) {
				try {
					response.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
		return content;
	}
	/**
	 * 
	 * @return
	 * @throws IOException
	 * @date 2018年6月20日 下午6:24:29 
	 * @author 获取登录需要的参数lt,
	 *
	 */
	public String  getStrLt() throws IOException{
		URL url=null;
		String uu="http://www.singlewindow.gz.cn/cas/login?service=http%3A%2F%2Ftrade.singlewindow.gz.cn%2Ftrade%2Fdefault.jsp&get-lt=true";
		//String url = "http://www.singlewindow.gz.cn/cas/login?service=http%3A%2F%2Ftrade.singlewindow.gz.cn%2Ftrade%2Fdefault.jsp&get-lt=true";  
	     // url=url+"&n="+time+"&_="+String.valueOf(System.currentTimeMillis());
		url = new URL(uu
				+ "&n="+String.valueOf(System.currentTimeMillis()+"&_="+String.valueOf(System.currentTimeMillis())));
	      HttpURLConnection httpURLConnection = (HttpURLConnection) url.openConnection();
	      httpURLConnection.setRequestMethod("GET");
	      httpURLConnection.setDoOutput(true);
	      httpURLConnection.setDoInput(true);
	      // 获取URLConnection对象对应的输出流
	      PrintWriter printWriter = new PrintWriter(httpURLConnection.getOutputStream());
	      // 发送请求参数
	      printWriter.write("122");//post的参数 xx=xx&yy=yy
	      // flush输出流的缓冲
	      printWriter.flush();
	      //开始获取数据
	      BufferedInputStream bis = new            BufferedInputStream(httpURLConnection.getInputStream());
	      ByteArrayOutputStream bos = new ByteArrayOutputStream();
	      int len;
	      byte[] arr = new byte[1024];
	      while((len=bis.read(arr))!= -1){
	          bos.write(arr,0,len);
	          bos.flush();
	      }
	      bos.close();
	    //  String arrDate[];
	      String s=bos.toString();    
	      String[] arr1=s.split("'");    
	      System.out.println("a="+arr1[1]+"b="+arr1[3]);
	      //arrDate[0]=arr1[1]
	    //  String arr[]=parUtil.getStrValue();
		   String lt=arr1[1];
		   String excetion=arr1[3];
		return lt;
	}
	/**
	 * 登录页面，拿到cookies
	 * @throws IOException 
	 */
	private  void loginCsdnPager(String username,String password) throws IOException {
		//  https://cas.singlewindow.gz.cn/login?service=https%3A%2F%2Ftrade.singlewindow.gz.cn%2Ftrade%2Fdefault.jsp
		String html = sendGet("https://cas.singlewindow.gz.cn/login?service=https%3A%2F%2Ftrade.singlewindow.gz.cn%2Ftrade%2Fdefault.jsp");// 这个是登录的页面
		Document doc = Jsoup.parse(html);
		System.out.println(doc);
		// 获取表单所在的节点
		if(doc.select("table.loginTable").size()>1){
			Element form = doc.select("table.loginTable").get(2);
			//System.out.println(form);
			// 以下三个是服务器给的标记信息，必须具有该信息登录才有效。
			String lt = form.select("input[name=lt]").get(0).val();
			String execution = form.select("input[name=execution]").get(0).val();
			String _eventId = form.select("input[name=_eventId]").get(0).val();

			// 开始构造登录的信息：账号、密码、以及三个标记信息
			String lt1=getStrLt();
			List<NameValuePair> nvps = new ArrayList<NameValuePair>();
			nvps.add(new BasicNameValuePair("service", "https://trade.singlewindow.gz.cn/trade/default.jsp"));
			nvps.add(new BasicNameValuePair("username", username));// usrname: shengzhantrading
			nvps.add(new BasicNameValuePair("isajax", "true"));
			nvps.add(new BasicNameValuePair("password", password));// password: 1039shengzhan
			nvps.add(new BasicNameValuePair("lt", lt));
			nvps.add(new BasicNameValuePair("execution", execution));
			nvps.add(new BasicNameValuePair("_eventId", _eventId));
			nvps.add(new BasicNameValuePair("callback", "feedBackUrlCallBack"));
			// 开始请求CSDN服务器进行登录操作。一个简单封装，直接获取返回结果
			String ret = sendPost(
					"https://cas.singlewindow.gz.cn/login?service=https%3A%2F%2Ftrade.singlewindow.gz.cn%2Ftrade%2Fdefault.jsp", nvps);
			//当请求登录成功后
			if (ret.indexOf("location.replace") > -1) {
				
				//获取登录后的cookies
				String url=ret.substring(ret.indexOf("'")+1,ret.indexOf(")")-1);
				try {
					Connection.Response res=(Response) Jsoup.connect(url).timeout(50000).data().method(Connection.Method.GET).execute();
					Map<String ,String> map=res.cookies();
					String jsonCookie="";
					String entValue="";
					for(Entry<String,String>entry:map.entrySet()) {
						System.out.println("key = "+entry.getKey()+"Value ="+entry.getValue());
						entValue=entry.getValue();
						jsonCookie=entry.getKey()+"="+entry.getValue()+";";
					}
					//请求抬头数据链接
					String taiTouUrl="https://trade.singlewindow.gz.cn/trade/api/DEC_E_HEAD_BLL/getCktjList?type=wm&_dc=1528861001777";
					  //获取请求抬头数据连接
			      //  Connection con = Jsoup.connect(taiTouUrl).cookie("JSESSIONID", entValue).ignoreContentType(true);
			        Map <String,Object> map1=new HashMap<String, Object>();
			        JSONObject alldata = new JSONObject();
					alldata.put("page", 1);
					alldata.put("start", 0);
					alldata.put("limit", 5);
					alldata.put("isDataFormat", true);
					JSONObject data = new JSONObject();
					
					//系统当天的前一天
					Date d = new Date();  
			        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd"); 
			        Calendar calendar = Calendar.getInstance();  
			        calendar.setTime(d);  
			        calendar.add(Calendar.DAY_OF_MONTH, -1);  
			        d = calendar.getTime();  
				    String dataT=sdf.format(d);
				    
					data.put("d_dateB", "2017-11-15");
					data.put("d_dateE", "2018-06-07");
					alldata.put("data", data);
					getMessageByPost(taiTouUrl, jsonCookie, alldata);
				} catch (Exception e) {
					logger.info(DateUtils.getDate("yyyy-MM-dd HH:mm:ss------")+username+"------ 爬虫登录获取cookies失败");
				}
				
				
			} else {
				logger.info(DateUtils.getDate("yyyy-MM-dd HH:mm:ss------")+username+"------ 登录失败，获取登录密钥失败");
				return;
			}
		}
		else{
			logger.info(DateUtils.getDate("yyyy-MM-dd HH:mm:ss------")+username+"------获取登录表头参数页面失败");
		}
		
	}
	/**
	 * 获取联网平台统计数据，并且保存到综台。
	 * @param urlStr
	 * @param headers
	 * @param object
	 * @return
	 * @date 2018年6月11日 上午11:32:19 
	 * @author cdr
	 *
	 */
	public  String getMessageByPost(String urlStr ,String headers,Object object) {
		//httpClient 发起post请求，(可以带请求体)
				HttpClient httpClient = new DefaultHttpClient();
		// 设置超时时间
	    httpClient.getParams().setParameter(CoreConnectionPNames.CONNECTION_TIMEOUT, 20000);
	    httpClient.getParams().setParameter(CoreConnectionPNames.SO_TIMEOUT, 20000);
		String result = "";
		HttpPost post = null;
	    try {
	        
	            
	        post = new HttpPost(urlStr);
	        // 构造消息头
	        post.setHeader("Content-type", "application/json; charset=utf-8");
	        post.setHeader("Connection", "Close");
	       /* for(String key : headers.keySet()){
	        	post.setHeader(key,headers.get(key));
	        }*/
	        post.setHeader("Cookie",headers);
	        // 构建消息实体
	        StringEntity entity = new StringEntity(object == null ? "" : object.toString());
	        entity.setContentEncoding("UTF-8");
	        // 发送Json格式的数据请求
	        entity.setContentType("application/json");
	        post.setEntity(entity);
	        HttpResponse response = httpClient.execute(post);
	        // 检验返回码
	        int statusCode = response.getStatusLine().getStatusCode();
	        System.out.println(statusCode);
	        HttpEntity he = response.getEntity();
	        result = EntityUtils.toString(he, "utf-8");
	        JSONObject jsonObj=new JSONObject(result);
	        //JSONObject jsonObj=new JSONObject().fromObject(result);
	        if(jsonObj.has("success")||jsonObj.getBoolean("success")) {
	        	JSONArray jsonArr=jsonObj.getJSONArray("data");
	        	if(jsonArr.length()>0) {
	        		//List <SmtStatisticEntity> smtStaticList=new ArrayList<SmtStatisticEntity>();
	        		for (int i = 0; i< jsonArr.length(); i++) {
	        			SmtStatisticEntity smtEntity= new SmtStatisticEntity();
	        			System.out.println(jsonArr.get(i));
	        			JSONObject jso2= (JSONObject)jsonArr.get(0);
	        			// JSONObject jso=jsonArr.getJSONObject(i);
	        			String str=jsonArr.get(i).toString();
	        			com.alibaba.fastjson.JSONObject jso=com.alibaba.fastjson.JSONObject.parseObject(str);
	        			smtEntity.setOrderNo(jso.getString("trading_no"));//交易单号
	        			smtEntity.setTotalPrice(jso.getDouble("decl_total_usd"));//总价（美元）
	        			//申报日期	        			
	        			if(jso.get("d_date")!=null){
	        				SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
	        				Date date=sdf.parse((String) jso.get("d_date"));
	        				smtEntity.setdDate(date);
	        			}
	        			//委托代理出口协议
	        			smtEntity.setContractNo(jso.getString("contract_no"));
	        			//抵运港
	        			smtEntity.setPortName(jso.getString("port_c_cod"));
	        			//出口日期
	        			System.out.println(jso.get("i_e_date")+"------");
	        			if(jso.get("i_e_date")!=null){
	        				SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
	        				Date date=sdf.parse((String) jso.get("i_e_date"));
	        				smtEntity.setExportDate(date);
	        			}
	        			//报关单号
	        			smtEntity.setEntryId(jso.getString("entry_id"));
	        			//货物名
	        			smtEntity.setGoodsName(jso.getString("g_name"));
	        			//成交数量
	        			smtEntity.setCamount(jso.getString("qty_1"));
	        			//交易单金额，折美元 
	        			smtEntity.setOrderPrice(jso.getString("usd_price"));
	        			smtEntity.setCusPrice(jso.getString("total_money"));
	        			if((Integer)jso.get("is_count")==0){
	        				smtEntity.setStatus("未统计");
	        			}
	        			else{
	        				smtEntity.setStatus("已统计");
	        			}
	        			//报关单状态
	        			smtEntity.setCusStatus(jso.getString("id_chk"));
	        			//结关日期
	        			if(jso.get("clearance_date")!=null){
	        				SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
	        				Date date=sdf.parse((String) jso.get("clearance_date"));
	        				smtEntity.setClearanceDate(date);
	        			}
	        			//hs_code
	        			smtEntity.setHsNode(jso.getString("hsCode"));
	        			//抵运港
	        			//smtEntity.setPortName(jso.getString("distinate_port"));
	        			//单价
	        			smtEntity.setPrice(jso.getDouble("decl_price"));
	        			//成交单位
	        			smtEntity.setCunitname(jso.getString("x_name"));
	        			//出口口岸
	        			smtEntity.setiEPort(jso.getString("customs_name"));
	        			//目的国
	        			smtEntity.setDestCountryName(jso.getString("country_na"));
	        			//放行日期
	        			if(jso.get("release_date")!=null){
	        				SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
	        				Date date=sdf.parse((String) jso.get("release_date"));
	        				smtEntity.setReleaseDate(date);
	        			}
	        			//外贸公司
	        			smtEntity.setProxyerName(jso.getString("trade_name"));
	        			smtEntity.setCreateName(jso.getString("trade_name"));
	        			//商家名称
	        			smtEntity.setCorpCname(jso.getString("corp_cname"));
	        		
	        		    //报关公司
	        		    smtEntity.setAgentName(jso.getString("agent_name"));
	        			systemService.save(smtEntity);
					}
	        		
	        		
	        	}
	        } else {
	        	logger.info(DateUtils.getDate("yyyy-MM-dd HH:mm:ss")+"账号****抓取失败");
	        }
	       // SmtStatisticEntity smtStatic=jsonObj.
	       
	    } catch (Exception e) {
	        e.printStackTrace();
	    }finally{
	    	logger.info("-------");
	    }
		return result;
	
		
	}
	
	
	/**
	 * 查询商户/企业备案状态
l	 */
	public void queryComp(){
		List<SmtCompanyEntity> loadAll = systemService.loadAll(SmtCompanyEntity.class);
	    JsonConfig config = new JsonConfig();
		 config.registerJsonValueProcessor(Date.class, new JsonDateValueProcessor());
		 net.sf.json.JSONArray jsonArray2 = net.sf.json.JSONArray.fromObject(loadAll, config);
		 net.sf.json.JSONObject jotime=jsonArray2.getJSONObject(0);
		 System.out.println(jotime);
	  
		List <SmtStaticUserEntity> staticUserlist=systemService.loadAll(SmtStaticUserEntity.class);
		if(staticUserlist!=null||staticUserlist.size()>0) {
			for (SmtStaticUserEntity smtStaticUserEntity : staticUserlist) {
				String user=smtStaticUserEntity.getUser();
				String password=smtStaticUserEntity.getPassword();
				try {
					loginCsdnPager(user,password);
				} catch (IOException e) {
					logger.info(DateUtils.getDate("yyyy-MM-dd HH:mm:ss")+" 爬虫登录异常");
					e.printStackTrace();
				}
			}
		}
		else{
			logger.info(DateUtils.getDate("yyyy-MM-dd HH:mm:ss")+" 无账号爬取");
		}
		
		logger.info(DateUtils.getDate("yyyy-MM-dd HH:mm:ss")+"----------商户备案状态查询接口调用-------");
		List<SmtCompanyEntity> list = this.commonDao.findByQueryString("from SmtCompanyEntity where status = 10002");
		if(list!=null && list.size()>0){
			for (SmtCompanyEntity smtCompany : list) {
				String response = this.subjectIntFaceFacade.queryTradeInfo("COMP", smtCompany.getRegNum());
				logger.info(response);
				if(StringUtils.isNotEmpty(response)){
					JSONObject jsonObject =new JSONObject(response);
					//正确请求
					if(jsonObject.get("result").equals("1")){
						JSONArray dataList = jsonObject.getJSONArray("dataList");
						if(dataList!=null && dataList.length() >0){
							JSONObject data= (JSONObject) dataList.get(0);
							String status = Constants_smt.SMT_COMPANY_STATUS_YTJ;
							if(data.get("statusCode").equals("1")){
								status = Constants_smt.SMT_COMPANY_STATUS_HSTG;
								//短信提醒  0:不发送，1:发送(默认)
								if(Integer.parseInt(ResourceUtil.getConfigByName("sms.tip.control"))==1){
									String content = "尊敬的"+smtCompany.getCnName()+",您在圣贸通外贸综合服务平台提交的1039商户备案资料，已核实通过。";
									try {
										SmsUtil.sendSms(content, smtCompany.getPhone());
									} catch (Exception e) {
										e.printStackTrace();
									}
									//SmsUtil.sendSms(content, "13416406116");
								}
							}else if(data.get("status").toString().indexOf("核实不通过")!=-1){
								status = Constants_smt.SMT_COMPANY_STATUS_HSBTG;
							}
							String sql = "update smt_company set status = ?,status_desc = ? where id = ?";
							this.executeSql(sql, new Object[]{status, data.get("status"), smtCompany.getId()});
						}
					}
				}
			}
		}
		logger.info(DateUtils.getDate("yyyy-MM-dd HH:mm:ss")+"----------企业备案状态查询接口调用-------");
		List<SmtBusinessEntity> list2 = this.commonDao.findByQueryString("from SmtBusinessEntity where status = 10002");
		if(list2!=null && list2.size()>0){
			for (SmtBusinessEntity smtBusinessEntity : list2) {
				String response = this.subjectIntFaceFacade.queryTradeInfo("COMP", smtBusinessEntity.getRegNum());
				System.out.println("企业备案："+response.toString());
				logger.info(response);
				if(StringUtils.isNotEmpty(response)){
					JSONObject jsonObject =new JSONObject(response);
					//正确请求
					if(jsonObject.get("result").equals("1")){
						JSONArray dataList = jsonObject.getJSONArray("dataList");
						if(dataList!=null && dataList.length() >0){
							JSONObject data= (JSONObject) dataList.get(0);
							String status = Constants_smt.SMT_COMPANY_STATUS_YTJ;
							if(data.get("statusCode").equals("1")){
								status = Constants_smt.SMT_COMPANY_STATUS_HSTG;
								//短信提醒  0:不发送，1:发送(默认)
								if(Integer.parseInt(ResourceUtil.getConfigByName("sms.tip.control"))==1){
									String content = "尊敬的"+smtBusinessEntity.getCnName()+",您在圣贸通外贸综合服务平台提交的1039企业备案资料，已核实通过。";
									try {
										SmsUtil.sendSms(content, smtBusinessEntity.getPhone());
									} catch (Exception e) {
										e.printStackTrace();
									}
									//SmsUtil.sendSms(content, "13416406116");
								}
							}else if(data.get("status").toString().indexOf("核实不通过")!=-1){
								status = Constants_smt.SMT_COMPANY_STATUS_HSBTG;
							}
							String sql = "update smt_business set status = ?,status_desc = ? where id = ?";
							this.executeSql(sql, new Object[]{status, data.get("status"), smtBusinessEntity.getId()});
						}
					}
				}
			}
		}
	}
	
	/**
	 * 查询商品备案状态
	 */
	public void queryGoods(){
		logger.info(DateUtils.getDate("yyyy-MM-dd HH:mm:ss")+"----------商品备案状态查询接口调用-------");
		List<SmtGoodsEntity> list = this.commonDao.findByQueryString("from SmtGoodsEntity where status = 02");
		if(list!=null && list.size()>0){
			for (SmtGoodsEntity smtGoodsEntity : list) {
				String response = this.subjectIntFaceFacade.queryTradeInfo("GOODS", smtGoodsEntity.getCgGoodsCode());
				logger.info(response);
				if(StringUtils.isNotEmpty(response)){
					JSONObject jsonObject =new JSONObject(response);
					//正确请求
					if(jsonObject.get("result").equals("1")){
						JSONArray dataList = jsonObject.getJSONArray("dataList");
						if(dataList!=null && dataList.length() >0){
							JSONObject data= (JSONObject) dataList.get(0);
							String sql = "update smt_goods set status = ? where id = ?";
							this.executeSql(sql, new Object[]{data.get("statusCode"), smtGoodsEntity.getId()});
						}
					}
				}
			}
		}
	}
	
	/**
	 * 查询报关单状态
	 */
	public void queryCustom(){
		logger.info(DateUtils.getDate("yyyy-MM-dd HH:mm:ss")+"----------报关单状态查询接口调用-------");
		List<SmtDecrationCusEntity> list = this.commonDao.findByQueryString("from SmtDecrationCusEntity where status != 'R' and status != '80001'");
		if(list!=null && list.size()>0){
			for (SmtDecrationCusEntity smtDecrationCus : list) {
				String response = this.subjectIntFaceFacade.queryTradeInfo("CUSTOM", smtDecrationCus.getNumber());
				logger.info(response);
				if(StringUtils.isNotEmpty(response)){
					JSONObject jsonObject =new JSONObject(response);
					//正确请求
					if(jsonObject.get("result").equals("1")){
						JSONArray dataList = jsonObject.getJSONArray("dataList");
						if(dataList!=null && dataList.length() >0){
							JSONObject data= (JSONObject) dataList.get(0);
							if(!data.get("statusCode").equals("0")&&!data.get("statusCode").equals("1")){//排除预录入和已发送状态
								String sql = "update smt_decration_cus set status = ?,status_desc = ? where id = ?";
								this.executeSql(sql, new Object[]{data.get("statusCode"), data.get("status"), smtDecrationCus.getId()});
								
								List<SmtReturnHistoryEntity> returnList = this.commonDao.findHql("from SmtReturnHistoryEntity where ywid = ? order by returnTime desc", smtDecrationCus.getId());
								if(returnList!=null && returnList.size()>0){
									SmtReturnHistoryEntity historyEntity = returnList.get(0);
									if(StringUtils.isNotEmpty(historyEntity.getStatus())){
										if(!historyEntity.getStatus().equals(data.get("statusCode"))){
											SmtReturnHistoryEntity t = new SmtReturnHistoryEntity();
											t.setReturnStatus(data.get("status").toString());
											t.setReturnMessage(data.get("status").toString());
											t.setStatus(data.get("statusCode").toString());
											t.setReturnTime(DateUtil.str2Date(DateUtils.date2Str(new Date(), DateUtils.datetimeFormat)));
											t.setYwid(smtDecrationCus.getId());
											this.save(t);
										}
									}
									else{
										SmtReturnHistoryEntity t = new SmtReturnHistoryEntity();
										t.setReturnStatus(data.get("status").toString());
										t.setReturnMessage(data.get("status").toString());
										t.setStatus(data.get("statusCode").toString());
										t.setReturnTime(DateUtil.str2Date(DateUtils.date2Str(new Date(), DateUtils.datetimeFormat)));
										t.setYwid(smtDecrationCus.getId());
										this.save(t);
									}
									
								}else{
									SmtReturnHistoryEntity t = new SmtReturnHistoryEntity();
									t.setReturnStatus(data.get("status").toString());
									t.setReturnMessage(data.get("status").toString());
									t.setStatus(data.get("statusCode").toString());
									t.setReturnTime(DateUtil.str2Date(DateUtils.date2Str(new Date(), DateUtils.datetimeFormat)));
									t.setYwid(smtDecrationCus.getId());
									this.save(t);
								}
							}
						}
					}
				}
			}
		}
	}
	
	/**
	 * @Title: execClientage
	 * @Description: 委托关系接口
	 * @author wushijie 
	 * @date 2017年10月18日 下午4:45:18
	 */
	public String execClientage(SmtMarketUserEntity user, String key) throws UnsupportedEncodingException{
		DesEcrypt asd =new DesEcrypt();
		int num=(int)(Math.random()*9000)+1000;
		String messageId = "CLIENTAGE_"+user.getUsername()+"_"+DateUtils.getDate("yyyyMMddHHmmss")+""+num;
		StringBuffer sb = new StringBuffer();
		sb.append("<?xml version='1.0' encoding='utf-8'?>\n");
		sb.append("<SubjectInfo>\n");
		sb.append("<Head>\n");
		sb.append("  <opType>A</opType>\n");
		sb.append("  <MessageId>").append(messageId).append("</MessageId>\n");
		sb.append("  <MessageType>CLIENTAGE</MessageType>\n");
		sb.append("  <Sender>").append(user.getUsername()).append("</Sender>\n");
		sb.append("  <Receiver>GZSW</Receiver>\n");
		sb.append("</Head>\n");
	    sb.append("<Declaration>\n");
	    sb.append("	 <Clientage>\n");
	    sb.append("  <sgsRegCode>").append(ResourceUtil.getConfigByName("corpCode")).append("</sgsRegCode>\n");
	    sb.append("  <corpName>").append(ResourceUtil.getConfigByName("corpName")).append("</corpName>\n");
	    sb.append("  <loginName>").append(asd.EncryStrHex(asd.GB2Code(ResourceUtil.getConfigByName("loginCode")), "GZMARKET2017")).append("</loginName>\n");
	    sb.append("  <loginPassWord>").append(asd.EncryStrHex(asd.GB2Code(ResourceUtil.getConfigByName("loginPassWord")), "GZMARKET2017")).append("</loginPassWord>\n");
	    sb.append("  <ClientageList>\n");
	    sb.append("  <ClientageDetail>\n");      
	    sb.append("  <sgsRegCode>").append(user.getRegNum()).append("</sgsRegCode>\n");
	    sb.append("  <corpName>").append(user.getCreateName()).append("</corpName>\n");
	    sb.append("  <loginName>").append(asd.EncryStrHex(asd.GB2Code(user.getUsername()), "GZMARKET2017")).append("</loginName>\n");
	    sb.append("  <loginPassWord>").append(asd.EncryStrHex(asd.GB2Code(CryptAES.AES_Decrypt(key, user.getPassword())), "GZMARKET2017")).append("</loginPassWord>\n");
	    sb.append("  </ClientageDetail>\n");
	    sb.append("  </ClientageList>\n");
	    sb.append("  </Clientage>\n");
	    sb.append("</Declaration>\n");
	    sb.append("</SubjectInfo>\n");
	    logger.info(DateUtils.getDate("yyyy-MM-dd HH:mm:ss")+" 委托关系Request："+sb.toString());
	    String response = this.subjectIntFaceFacade.sendDeclaration(ResourceUtil.getConfigByName("corpCode"),ResourceUtil.getConfigByName("corpName"),ResourceUtil.getConfigByName("loginCode"),ResourceUtil.getConfigByName("loginPassWord"), sb.toString());
	    logger.info(DateUtils.getDate("yyyy-MM-dd HH:mm:ss")+" 委托关系Response："+response);
		return response;
	}
	
	/**
	 * @Title: findGoodsInfo
	 * @Description: 同步商品历史数据到本地
	 * @author wushijie 
	 * @throws Exception 
	 * @date 2017年10月19日 上午11:57:11
	 */
	public int findGoodsInfo(SmtCompanyEntity smtCompany, String goodsCode, String goodsName, String dateBegin, String dateEnd) throws Exception{
		int num=(int)(Math.random()*9000)+1000;
		String messageId = "GOODSINFO_"+smtCompany.getCreateBy()+"_"+DateUtils.getDate("yyyyMMddHHmmss")+""+num;
		StringBuffer sb = new StringBuffer();
		sb.append("<?xml version='1.0' encoding='utf-8'?>\n");
		sb.append("<SubjectInfo>\n");
		sb.append("<Head>\n");
		sb.append("  <opType>A</opType>\n");
		sb.append("  <MessageId>").append(messageId).append("</MessageId>\n");
		sb.append("  <MessageType>GOODSINFO</MessageType>\n");
		sb.append("  <Sender>").append(smtCompany.getCreateBy()).append("</Sender>\n");
		sb.append("  <Receiver>GZSW</Receiver>\n");
		sb.append("</Head>\n");
	    sb.append("<Declaration>\n");
	    sb.append("	 <GoodsRecordInfo>\n");
	    sb.append("  <goodsCode>").append(goodsCode).append("</goodsCode>\n");
	    sb.append("  <createOrg>").append(smtCompany.getRegNum()).append("</createOrg>\n");
	    sb.append("  <goodsName>").append(goodsName).append("</goodsName>\n");
	    sb.append("  <dateBegin>").append(dateBegin).append("</dateBegin>\n");
	    sb.append("  <goodsStatus>03</goodsStatus>\n");
	    sb.append("  <dateEnd>").append(dateEnd).append("</dateEnd>\n");
	    sb.append("  </GoodsRecordInfo>\n");
	    sb.append("</Declaration>\n");
	    sb.append("</SubjectInfo>\n");
	    logger.info(DateUtils.getDate("yyyy-MM-dd HH:mm:ss")+" 商品历史数据查询Request："+sb.toString());
	    String response = this.subjectIntFaceFacade.sendDeclaration(ResourceUtil.getConfigByName("corpCode"),ResourceUtil.getConfigByName("corpName"),ResourceUtil.getConfigByName("loginCode"),ResourceUtil.getConfigByName("loginPassWord"), sb.toString());
	    logger.info(DateUtils.getDate("yyyy-MM-dd HH:mm:ss")+" 商品历史数据查询Response："+response);
	    if(StringUtils.isEmpty(response)){
	    	throw new Exception("市场采购贸易系统未返回回执。"); 
	    }
	    JSONObject jsonObject =new JSONObject(response);
	    if(!jsonObject.isNull("errorMessage")){
	    	throw new Exception(jsonObject.getString("errorMessage")); 
	    }
	    JSONArray jsonArray = jsonObject.getJSONArray("otherMessage");
	    if(jsonArray!=null && jsonArray.length()>0){
	    	//将单例嘻哈表写入数据条数
			ProgressSingleton.put(smtCompany.getCreateBy()+"Size", jsonArray.length());
			int progress=0;
	    	 for (int i = 0; i < jsonArray.length(); i++) {
					JSONObject goods = (JSONObject)jsonArray.get(i);
					///////
					SmtGoodsEntity smtGoods = new SmtGoodsEntity();
					smtGoods.setCgGoodsCode(goods.get("goodsCode").toString());
					smtGoods.setGoodsName(goods.isNull("goodsCname")?"":goods.get("goodsCname").toString());
					smtGoods.setGoodsEnName(goods.isNull("goodsEname")?"":goods.get("goodsEname").toString());
					smtGoods.setHsCode(goods.isNull("hscode")?"":goods.get("hscode").toString());
					smtGoods.setExtraCode(goods.isNull("extraCode")?"":goods.get("extraCode").toString());
					smtGoods.setSpec(goods.isNull("model")?"":goods.get("model").toString());
					smtGoods.setGoodsClassify(goods.isNull("goodsType")?"":goods.get("goodsType").toString());
					smtGoods.setProducer(goods.isNull("producerName")?"":goods.get("producerName").toString());
					smtGoods.setProducerId(goods.isNull("producer")?"":goods.get("producer").toString());
					smtGoods.setIsAuthorized(goods.isNull("isBrand")?"":goods.get("isBrand").toString());
					smtGoods.setCnBrand(goods.isNull("CBrand")?"":goods.get("CBrand").toString());
					smtGoods.setEnBrand(goods.isNull("EBrand")?"":goods.get("EBrand").toString());
					smtGoods.setWeightUnit(goods.isNull("qunitName")?"":goods.get("qunitName").toString());
					smtGoods.setWeightUnitCode(goods.isNull("qunit")?"":goods.get("qunit").toString());
					smtGoods.setAmountUnit(goods.isNull("cunitName")?"":goods.get("cunitName").toString());
					smtGoods.setAmountUnitCode(goods.isNull("cunit")?"":goods.get("cunit").toString());
					smtGoods.setSecondUnit(goods.isNull("wunitName")?"":goods.get("wunitName").toString());
					smtGoods.setSecondUnitCode(goods.isNull("wunit")?"":goods.get("wunit").toString());
					smtGoods.setSelfNum(goods.isNull("qyZyCode")?"":goods.get("qyZyCode").toString());
					smtGoods.setStatus(goods.isNull("auditStatus")?"":goods.get("auditStatus").toString());
					smtGoods.setCreateDate(DateUtils.str2Date(goods.get("createTime").toString(),DateUtils.date_sdf));
					smtGoods.setRemark(goods.isNull("remark")?"":goods.get("remark").toString());
					
					String sql = "select id from smt_goods where create_by = ? and cg_goods_code = ?";
					List<Map<String, Object>> list = this.findForJdbc(sql, new Object[]{smtCompany.getCreateBy(), goods.get("goodsCode")});
					if(list == null || list.size() == 0){
						this.save(smtGoods);
					}else if(list.size()>0){
						//更新旧商品信息
						String updateId=(String) list.get(0).get("id");
						SmtGoodsEntity oldGoods= this.getEntity(SmtGoodsEntity.class,updateId);
						MyBeanUtils.copyBeanNotNull2Bean(smtGoods,oldGoods);
						this.saveOrUpdate(oldGoods);
					}
					//更新进度
					progress=progress+1;
					//写入进度
					ProgressSingleton.put(smtCompany.getCreateBy()+"Progress", progress);
				}
	     }  
	    //同步完成之后，从单例中移除本次同步状态信息
	     ProgressSingleton.remove(smtCompany.getCreateBy()+"Size");
	     ProgressSingleton.remove(smtCompany.getCreateBy()+"Progress");
	    return jsonArray.length();
	}
	
	/**
	 * @Title: findManufactur
	 * @Description: 同步生产商数据到本地
	 * @author wushijie 
	 * @date 2017年10月20日 下午3:03:34
	 */
	public int findManufactur(SmtCompanyEntity smtCompany) throws Exception{
		int num=(int)(Math.random()*9000)+1000;
		String messageId = "MANUFACTUR_"+smtCompany.getCreateBy()+"_"+DateUtils.getDate("yyyyMMddHHmmss")+""+num;
		StringBuffer sb = new StringBuffer();
		sb.append("<?xml version='1.0' encoding='utf-8'?>\n");
		sb.append("<SubjectInfo>\n");
		sb.append("<Head>\n");
		sb.append(" <opType>A</opType>\n");
		sb.append(" <MessageId>").append(messageId).append("</MessageId>\n");
		sb.append(" <MessageType>MANUFACTUR</MessageType>\n");
		sb.append(" <Sender>").append(smtCompany.getCreateBy()).append("</Sender>\n");
		sb.append(" <Receiver>GZSW</Receiver>\n");
		sb.append("</Head>\n");
		sb.append("<Declaration>\n");
		sb.append("	<ManufacturerInfo>\n");
		sb.append(" <corpCode></corpCode>\n");
		sb.append(" <createOrg>").append(smtCompany.getRegNum()).append("</createOrg>\n");
		sb.append(" <corpCname></corpCname>\n");
		sb.append(" <companyType></companyType>\n");
		sb.append(" <dateBegin></dateBegin>\n");
		sb.append(" <dateEnd></dateEnd>\n");
		sb.append(" </ManufacturerInfo>\n");
		sb.append("</Declaration>\n");
		sb.append("</SubjectInfo>");
		logger.info(DateUtils.getDate("yyyy-MM-dd HH:mm:ss")+" 生产商历史数据查询Request："+sb.toString());
		String response = this.subjectIntFaceFacade.sendDeclaration(ResourceUtil.getConfigByName("corpCode"),ResourceUtil.getConfigByName("corpName"),ResourceUtil.getConfigByName("loginCode"),ResourceUtil.getConfigByName("loginPassWord"), sb.toString());
		logger.info(DateUtils.getDate("yyyy-MM-dd HH:mm:ss")+" 生产商历史数据查询Response："+response);
		if(StringUtils.isEmpty(response)){
		    throw new Exception("市场采购贸易系统未返回回执。"); 
		}
		JSONObject jsonObject =new JSONObject(response);
	    JSONArray jsonArray = jsonObject.getJSONArray("otherMessage");
	    if(jsonArray!=null && jsonArray.length()>0){
	    	 for (int i = 0; i < jsonArray.length(); i++) {
					JSONObject producer = (JSONObject)jsonArray.get(i);
					SmtProducerEntity smtProducer = new SmtProducerEntity();
					smtProducer.setOrgCode(producer.get("corpCode").toString());
					smtProducer.setCnName(producer.isNull("corpCname")?"":producer.get("corpCname").toString());
					smtProducer.setEnName(producer.isNull("corpEname")?"":producer.get("corpEname").toString());
					smtProducer.setComType(producer.isNull("companyType")?"":producer.get("companyType").toString());
					smtProducer.setContacts(producer.isNull("contractMan")?"":producer.get("contractMan").toString());
					smtProducer.setCertNum(producer.isNull("identCode")?"":producer.get("identCode").toString());
					smtProducer.setPhone(producer.isNull("telno")?"":producer.get("telno").toString());
					smtProducer.setComAddress(producer.isNull("caddress")?"":producer.get("caddress").toString());
					smtProducer.setStatus("1");
					smtProducer.setCreateDate(DateUtils.str2Date(producer.get("createTime").toString(),DateUtils.datetimeFormat));
					
					String sql = "select id from smt_producer where create_by = ? and org_code = ?";
					List<Map<String, Object>> list = this.findForJdbc(sql, new Object[]{smtCompany.getCreateBy(), producer.get("corpCode")});
					if(list == null || list.size() == 0){
						this.save(smtProducer);
					}else if(list.size()>0){
						//更新旧信息
						String updateId=(String) list.get(0).get("id");
						SmtProducerEntity oldProducer= this.getEntity(SmtProducerEntity.class,updateId);
						MyBeanUtils.copyBeanNotNull2Bean(smtProducer,oldProducer);
						this.saveOrUpdate(oldProducer);
					}
				}
	     }  
	    return jsonArray.length();
	}
	
	/**
	 * @Title: findBuyer
	 * @Description: 同步采购商数据到本地
	 * @author wushijie 
	 * @date 2017年10月20日 下午4:25:44
	 */
	public int findBuyer(SmtCompanyEntity smtCompany) throws Exception{
		int num=(int)(Math.random()*9000)+1000;
		String messageId = "PURCHASER_"+smtCompany.getCreateBy()+"_"+DateUtils.getDate("yyyyMMddHHmmss")+""+num;
		StringBuffer sb = new StringBuffer();
		sb.append("<?xml version='1.0' encoding='utf-8'?>\n");
		sb.append("<SubjectInfo>\n");
		sb.append("<Head>\n");
		sb.append(" <opType>A</opType>\n");
		sb.append(" <MessageId>").append(messageId).append("</MessageId>\n");
		sb.append(" <MessageType>PURCHASER</MessageType>\n");
		sb.append(" <Sender>").append(smtCompany.getCreateBy()).append("</Sender>\n");
		sb.append(" <Receiver>GZSW</Receiver>\n");
		sb.append("</Head>\n");
		sb.append("<Declaration>\n");
		sb.append("	<PurchaserInfo>\n");
		sb.append(" <corpCode></corpCode>\n");
		sb.append(" <createOrg>").append(smtCompany.getRegNum()).append("</createOrg>\n");
		sb.append(" <corpCname></corpCname>\n");
		sb.append(" <companyType></companyType>\n");
		sb.append(" <dateBegin></dateBegin>\n");
		sb.append(" <dateEnd></dateEnd>\n");
		sb.append(" </PurchaserInfo>\n");
		sb.append("</Declaration>\n");
		sb.append("</SubjectInfo>");
		logger.info(DateUtils.getDate("yyyy-MM-dd HH:mm:ss")+" 生产商历史数据查询Request："+sb.toString());
		String response = this.subjectIntFaceFacade.sendDeclaration(ResourceUtil.getConfigByName("corpCode"),ResourceUtil.getConfigByName("corpName"),ResourceUtil.getConfigByName("loginCode"),ResourceUtil.getConfigByName("loginPassWord"), sb.toString());
		logger.info(DateUtils.getDate("yyyy-MM-dd HH:mm:ss")+" 生产商历史数据查询Response："+response);
		if(StringUtils.isEmpty(response)){
		    throw new Exception("市场采购贸易系统未返回回执。"); 
		}
		JSONObject jsonObject =new JSONObject(response);
	    JSONArray jsonArray = jsonObject.getJSONArray("otherMessage");
	    if(jsonArray!=null && jsonArray.length()>0){
	    	 for (int i = 0; i < jsonArray.length(); i++) {
					JSONObject buyer = (JSONObject)jsonArray.get(i);
					SmtBuyerEntity smtBuyer = new SmtBuyerEntity();
					smtBuyer.setCertNum(buyer.get("corpCode").toString());
					smtBuyer.setComName(buyer.isNull("corpCname")?"":buyer.get("corpCname").toString());
					smtBuyer.setComType(buyer.isNull("companyType")?"":buyer.get("companyType").toString());
					smtBuyer.setContacts(buyer.isNull("contractMan")?"":buyer.get("contractMan").toString());
					smtBuyer.setCountry(buyer.isNull("finvest1")?"":buyer.get("finvest1").toString());
					smtBuyer.setPhone(buyer.isNull("telno")?"":buyer.get("telno").toString());
					smtBuyer.setComAddress(buyer.isNull("caddress")?"":buyer.get("caddress").toString());
					smtBuyer.setStatus("1");
					smtBuyer.setCreateDate(DateUtils.str2Date(buyer.get("createTime").toString(),DateUtils.datetimeFormat));
					
					String sql = "select id from smt_buyer where create_by = ? and cert_num = ?";
					List<Map<String, Object>> list = this.findForJdbc(sql, new Object[]{smtCompany.getCreateBy(), buyer.get("corpCode")});
					if(list == null || list.size() == 0){
						this.save(smtBuyer);
					}else if(list.size()>0){
						//更新旧信息
						String updateId=(String) list.get(0).get("id");
						SmtBuyerEntity oldBuyer= this.getEntity(SmtBuyerEntity.class,updateId);
						MyBeanUtils.copyBeanNotNull2Bean(smtBuyer,oldBuyer);
						this.saveOrUpdate(oldBuyer);
					}
				}
	     }  
	    return jsonArray.length();
	}
	
	
	/**
	 * 同步 交易登记数据到本地
	 * @title orderList,
	 * @author LaiFuwei
	 * @date 2017-11-16 
	 * @param smtCompany 人员Entity对象
	 * @return Map<String,String>
	 * @throws Exception 
	 */
	public Map<String,String> findOrderInfo(SmtMarketUserEntity user,List<String> queryLs) throws Exception{
		Map<String,String> resMap=new HashMap<String,String>();
		int addCount=0;//添加条数
		int updateCount=0;//更新条数
		/*
		 *交易单表头数据同步 
		 *拼接表头xml
		 */
		String headXml=QueryUtil.getHeadXml(user, queryLs, "2");
		//===========================
		logger.info(DateUtils.getDate("yyyy-MM-dd HH:mm:ss")+" 交易单历史表头数据查询Request："+headXml.toString());
		String response = this.subjectIntFaceFacade.sendDeclaration(ResourceUtil.getConfigByName("corpCode"),ResourceUtil.getConfigByName("corpName"),ResourceUtil.getConfigByName("loginCode"),ResourceUtil.getConfigByName("loginPassWord"), headXml);
		//logger.info(DateUtils.getDate("yyyy-MM-dd HH:mm:ss")+" 交易单历史表头数据查询Response："+response);
		if(StringUtils.isEmpty(response)){
		    throw new Exception("市场采购贸易系统未返回回执！"); 
		}
		JSONObject HeadJsonObj =new JSONObject(response);
		//查询错误返回结果
		if(Integer.parseInt(HeadJsonObj.get("result").toString())==0){
			String message="";
			if(HeadJsonObj.has("errorMessage")){
				JSONObject dataJson=HeadJsonObj.getJSONArray("errorMessage").getJSONObject(0);
				message=dataJson.toString();					
			}else if(HeadJsonObj.has("message")){
				message=HeadJsonObj.getJSONArray("message").toString();
			}
			resMap.put("errorMessage", message);
			resMap.put("result", "0");
			logger.info(DateUtils.getDate("yyyy-MM-dd HH:mm:ss")+"交易历史表头数据同步查询ErrorMessage："+HeadJsonObj.toString());
			return resMap;
		}else if(HeadJsonObj.has("otherMessage")){
			JSONArray HeadJsonArr = HeadJsonObj.getJSONArray("otherMessage");
			//将单例嘻哈表写入数据条数
			ProgressSingleton.put(user.getUsername()+"Size", HeadJsonArr.length());
			int progress=0;
		    	 for (int i = 0; i < HeadJsonArr.length(); i++) {
						JSONObject orderEntityJson = (JSONObject)HeadJsonArr.get(i);
						//订单号
						String orderNo=orderEntityJson.getString("tradingNo");
						
						//存储查询订单表头实体信息
						SmtOrderEntity smtOrder = new SmtOrderEntity();
						
						if(orderEntityJson.get("createTime") instanceof String||orderEntityJson.get("createTime") instanceof Date){
							smtOrder.setCreateDate(DateUtils.str2Date(orderEntityJson.get("createTime").toString(),DateUtils.datetimeFormat));
						}
						if(orderEntityJson.get("auditTime") instanceof String||orderEntityJson.get("auditTime") instanceof Date){
							smtOrder.setUpdateDate(DateUtils.str2Date(orderEntityJson.get("auditTime").toString(),DateUtils.datetimeFormat));
						}
						smtOrder.setOrderSn(orderNo);
						smtOrder.setBuyerStore(orderEntityJson.getString("buyerName"));
						smtOrder.setSupplier(orderEntityJson.getString("solderName"));
						smtOrder.setGoodsStore(orderEntityJson.getString("proxyerName"));
						smtOrder.setGroupComId(orderEntityJson.getString("proxyer"));
						smtOrder.setNote(QueryUtil.set(orderEntityJson.get("remark").toString()));
						smtOrder.setStatus(QueryUtil.getOrderStatus(orderEntityJson.getString("status")));
						smtOrder.setOrderTime(orderEntityJson.getString("createTime"));
						smtOrder.setBuyerId(orderEntityJson.getString("buyer"));
						//免税申报方式
						smtOrder.setApplyWay(orderEntityJson.get("mssbType").toString());
						smtOrder.setSupplierId(user.getCreateBy());
						smtOrder.setIsAgencyReceipt(orderEntityJson.getString("is_dl_sh_flag"));
						if(orderEntityJson.get("jn_rmb_money") instanceof Double||orderEntityJson.get("jn_rmb_money") instanceof Integer){
							smtOrder.setRmbMoney(orderEntityJson.getDouble("jn_rmb_money"));
						}
						smtOrder.setProxyer(orderEntityJson.getString("proxyer"));
						smtOrder.setSorderSn(orderNo);
						smtOrder.setIsEntry("1");
						smtOrder.setWay("000");
						
						//=============================================
						/*
						 * 同步添加对应交易单明细
						 */
						String OrderDetailXml=QueryUtil.getDetailXml(user,orderNo,"3");//获取交易单明细请求xml
						String responseDetail = this.subjectIntFaceFacade.sendDeclaration(ResourceUtil.getConfigByName("corpCode"),ResourceUtil.getConfigByName("corpName"),ResourceUtil.getConfigByName("loginCode"),ResourceUtil.getConfigByName("loginPassWord"),OrderDetailXml);
						//logger.info(DateUtils.getDate("yyyy-MM-dd HH:mm:ss")+" 交易单"+orderNo+"明细数据查询Response："+responseDetail);
						if(StringUtils.isEmpty(responseDetail)){
						    throw new Exception("交易单明细:市场采购贸易系统未返回回执！"); 
						}
						JSONObject detailJsonObj =new JSONObject(responseDetail);
						
						
						List<SmtOrderDetailEntity> detailLsEntity=new ArrayList<SmtOrderDetailEntity>();
						
						if(Integer.parseInt(detailJsonObj.get("result").toString())==0){
							String message="";
							if(detailJsonObj.has("errorMessage")){
								JSONObject dataJson=detailJsonObj.getJSONArray("errorMessage").getJSONObject(0);
								message=dataJson.toString();					
							}else if(detailJsonObj.has("message")){
								message=detailJsonObj.getJSONArray("message").toString();
							}
							resMap.put("errorMessage", message);
							resMap.put("result", "0");
							logger.info(DateUtils.getDate("yyyy-MM-dd HH:mm:ss")+"交易历史表头数据同步查询ErrorMessage："+detailJsonObj.toString());
							//return resMap;
						}else if(detailJsonObj.has("otherMessage")){
							//明细查询成功
							JSONArray detailJsonArr = detailJsonObj.getJSONArray("otherMessage");
						    	 for (int z= 0; z < detailJsonArr.length(); z++) {
										JSONObject detailGoods = (JSONObject)detailJsonArr.get(z);
										SmtOrderDetailEntity orderDetailEntity=new SmtOrderDetailEntity();
												
										if(orderEntityJson.get("createTime") instanceof String||orderEntityJson.get("createTime") instanceof Date){
											orderDetailEntity.setCreateDate(DateUtils.str2Date(orderEntityJson.get("createTime").toString(),DateUtils.datetimeFormat));
										}
										if(orderEntityJson.get("auditTime") instanceof String||orderEntityJson.get("auditTime") instanceof Date){
											orderDetailEntity.setUpdateDate(DateUtils.str2Date(orderEntityJson.get("auditTime").toString(),DateUtils.datetimeFormat));
										}
										orderDetailEntity.setGoodsId(detailGoods.getString("goodsInnerCode"));
										orderDetailEntity.setGoodsName(detailGoods.getString("goodsName"));
										orderDetailEntity.setGoodsNum(detailGoods.get("camount")+"");
										orderDetailEntity.setLegalNum(detailGoods.get("quantity")+"");
										if(detailGoods.get("price") instanceof Double||detailGoods.get("price") instanceof Integer){
											orderDetailEntity.setGoodsPrice( detailGoods.getDouble("price"));
										}
										if(detailGoods.get("totalPrice") instanceof Double||detailGoods.get("totalPrice") instanceof Integer){
											orderDetailEntity.setTotalAmount(detailGoods.getDouble("totalPrice"));
										}
										orderDetailEntity.setCurrency(QueryUtil.getCurrency(detailGoods.getString("ccyCode")));
										orderDetailEntity.setGoodsUnit(detailGoods.getString("cunitname"));
										orderDetailEntity.setSpec(detailGoods.getString("model"));
										orderDetailEntity.setSecondNum(QueryUtil.set(detailGoods.get("weight").toString()));
										orderDetailEntity.setLegalUnit(detailGoods.getString("quantityUnit"));
										orderDetailEntity.setSecondUnit(QueryUtil.set(detailGoods.get("weightUnit").toString()));
										detailLsEntity.add(orderDetailEntity);
									}
						}
						//=========================================
						String sql = "select id from smt_order where order_sn = ? and create_by= ?";
						List<Map<String, Object>> list = this.findForJdbc(sql, new Object[]{orderNo,user.getUsername()});
						//System.out.println("LSsize()==="+list.size()+"==="+list);
						if(list == null || list.size() == 0){
							//添加新的交易單
							smtOrderService.addMain(smtOrder, detailLsEntity);//(smtOrder,detailLsEntity);
							addCount++;
						}else if(list.size()>0){
							//更新旧交易单信息
							String updateId=(String) list.get(0).get("id");
							SmtOrderEntity oldorder= smtOrderService.getEntity(SmtOrderEntity.class,updateId);
							MyBeanUtils.copyBeanNotNull2Bean(smtOrder,oldorder);
							smtOrderService.updateMain(oldorder, detailLsEntity);
							updateCount++;
						}
					//更新进度
					progress=progress+1;
					//写入进度
					ProgressSingleton.put(user.getUsername()+"Progress", progress);
		          } 
		     //同步完成之后，从单例中移除本次同步状态信息
		     ProgressSingleton.remove(user.getUsername()+"Size");
		     ProgressSingleton.remove(user.getUsername()+"Progress");
		     resMap.put("result","1");
		     resMap.put("success","同步数据：添加数量"+addCount+"条，更新数据"+updateCount+"条。");
		}
		return resMap;
	}

	
	/**
	 * 同步组货历史数据到本地
	 * @param key
	 * @param user
	 * @param queryLs
	 * @return
	 * @throws Exception
	 * @date 2017年11月22日 上午10:54:05 
	 * @author laifuwei
	 */
	public Map<String, String> findGroupInfo(SmtMarketUserEntity user, List<String> queryLs)
			throws Exception {
			Map<String,String> resMap=new HashMap<String,String>();
			int addCount=0;//添加条数
			int updateCount=0;//更新条数
			/*
			 *组货单表头数据同步 
			 *拼接查询表头xml
			 */
			 String headXml=QueryUtil.getHeadXml(user, queryLs, "4");
	    	//=================
			//===========================
				String response = this.subjectIntFaceFacade.sendDeclaration(ResourceUtil.getConfigByName("corpCode"),ResourceUtil.getConfigByName("corpName"),ResourceUtil.getConfigByName("loginCode"),ResourceUtil.getConfigByName("loginPassWord"), headXml);
				//logger.info(DateUtils.getDate("yyyy-MM-dd HH:mm:ss")+" 组货历史表头数据查询Response："+response);
				if(StringUtils.isEmpty(response)){
				    throw new Exception("市场采购贸易系统未返回回执！"); 
				}
				JSONObject HeadJsonObj =new JSONObject(response);
				//查询错误返回结果
				if(Integer.parseInt(HeadJsonObj.get("result").toString())==0){
					String message="";
					if(HeadJsonObj.has("errorMessage")){
						JSONObject dataJson=HeadJsonObj.getJSONArray("errorMessage").getJSONObject(0);
						message=dataJson.toString();					
					}else if(HeadJsonObj.has("message")){
						message=HeadJsonObj.getJSONArray("message").toString();
					}
					resMap.put("errorMessage", message);
					resMap.put("result", "0");
					logger.info(DateUtils.getDate("yyyy-MM-dd HH:mm:ss")+"组货历史表头数据同步查询ErrorMessage："+HeadJsonObj.toString());
					return resMap;
				}else if(HeadJsonObj.has("otherMessage")){
					SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");

					JSONArray HeadJsonArr = HeadJsonObj.getJSONArray("otherMessage");
					//将单例嘻哈表写入数据条数
					ProgressSingleton.put(user.getUsername()+"Size", HeadJsonArr.length());
					int progress=0;
					
				    	 for (int i = 0; i < HeadJsonArr.length(); i++) {
								JSONObject GroupEntityJson = (JSONObject)HeadJsonArr.get(i);
								//组货单号
								String groupNo=GroupEntityJson.getString("orderCode");
								
								//存储查询组货单表头实体信息
								SmtGroupEntity smtGroup = new SmtGroupEntity();
								
								if(GroupEntityJson.get("createTime") instanceof String||GroupEntityJson.get("createTime") instanceof Date){
									//smtGroup.setCreateDate(DateUtils.str2Date(GroupEntityJson.get("createTime").toString(),DateUtils.datetimeFormat));
									String createTime = GroupEntityJson.get("createTime").toString();
									smtGroup.setCreateDate(DateUtils.getDate());
									smtGroup.setCreateTime(sdf.parse(createTime));
									//smtGroup.setCreateTime(DateUtils.str2Date(GroupEntityJson.get("createTime").toString(),DateUtils.datetimeFormat));
								}
								//组货单号
								smtGroup.setGroupSn(groupNo);
								String groupSql = "select create_by from smt_business where create_name = ?";
								List<Map<String, Object>> groupMapList = systemService.findForJdbc(groupSql, new Object[]{GroupEntityJson.getString("proxyerName")});
								if (groupMapList != null && groupMapList.size() > 0) {
									Map<String,Object> groupMap = groupMapList.get(0);
									smtGroup.setDeputyId(groupMap.get("create_by").toString());
								}
								//smtGroup.setDeputyId(GroupEntityJson.getString("proxyer"));
								smtGroup.setDeputyName(GroupEntityJson.getString("proxyerName"));
								//目的地
								smtGroup.setDestCountry(GroupEntityJson.getString("destCountry"));
								smtGroup.setDestCountryName(GroupEntityJson.getString("destCountryName"));
								
								smtGroup.setPlateNum(QueryUtil.set(GroupEntityJson.get("carno").toString()));
								smtGroup.setStatus(QueryUtil.getGroupStatus(GroupEntityJson.getString("auditStatus")));
								smtGroup.setNote(QueryUtil.set(GroupEntityJson.get("remark").toString()));
								smtGroup.setProxyer(GroupEntityJson.getString("proxyer"));
								smtGroup.setSgroupSn(groupNo);
								smtGroup.setWay("000");
								
								//=============================================
								/*
								 * 查询组货单明细xml
								 */
								String detailXml=QueryUtil.getDetailXml(user, groupNo,"5");
								String responseDetail = this.subjectIntFaceFacade.sendDeclaration(ResourceUtil.getConfigByName("corpCode"),ResourceUtil.getConfigByName("corpName"),ResourceUtil.getConfigByName("loginCode"),ResourceUtil.getConfigByName("loginPassWord"),detailXml);
								//logger.info(DateUtils.getDate("yyyy-MM-dd HH:mm:ss")+" 交易单"+orderNo+"明细数据查询Response："+responseDetail);
								if(StringUtils.isEmpty(responseDetail)){
								    throw new Exception("组货单明细:市场采购贸易系统未返回回执！"); 
								}
								JSONObject detailJsonObj =new JSONObject(responseDetail);
								
								
								List<SmtGroupDetailEntity> detailLsEntity=new ArrayList<SmtGroupDetailEntity>();
								
								if(Integer.parseInt(detailJsonObj.get("result").toString())==0){
									JSONObject errJson=detailJsonObj.getJSONArray("errorMessage").getJSONObject(0);
									//resMap.put("errorDetail", errJson.toString());
									//resMap.put("result", "0");
									logger.info(DateUtils.getDate("yyyy-MM-dd HH:mm:ss")+" 组货单历史"+groupNo+"明细据同步查询ErrorDetail："+errJson.toString());
									//return resMap;
								}else if(detailJsonObj.has("otherMessage")){
									//明细查询成功
									JSONArray detailJsonArr = detailJsonObj.getJSONArray("otherMessage");
								    	 for (int z= 0; z < detailJsonArr.length(); z++) {
												JSONObject details = (JSONObject)detailJsonArr.get(z);
												SmtGroupDetailEntity groupDetailEntity=new SmtGroupDetailEntity();
														
												if(details.get("createTime") instanceof String||details.get("createTime") instanceof Date){
													groupDetailEntity.setCreateDate(DateUtils.str2Date(details.get("createTime").toString(),DateUtils.date_sdf));
													groupDetailEntity.setCreateTime(DateUtils.str2Date(details.get("createTime").toString(),DateUtils.date_sdf));
												}
												//==detail
												groupDetailEntity.setGroupId(groupNo);
												
												groupDetailEntity.setOrderId(details.getString("tradingNo"));
												groupDetailEntity.setOrderSn(details.getString("tradingNo"));
												
												groupDetailEntity.setSupplierId(details.getString("solderCode"));
												
												detailLsEntity.add(groupDetailEntity);
											}
								}
								//=========================================
								String sql = "select id from smt_group where group_sn = ? and create_by= ?";
								List<Map<String, Object>> list = this.findForJdbc(sql, new Object[]{groupNo,user.getUsername()});
								//System.out.println("LSsize()==="+list.size()+"==="+list);
								if(list == null || list.size() == 0){
									//添加新的组货單
									smtGroupService.addMain(smtGroup, detailLsEntity);//(smtOrder,detailLsEntity);
									addCount++;
								}else if(list.size()>0){
									//更新旧组货单信息
									String updateId=(String) list.get(0).get("id");
									SmtGroupEntity olds= smtGroupService.getEntity(SmtGroupEntity.class,updateId);
									MyBeanUtils.copyBeanNotNull2Bean(smtGroup,olds);
									smtGroupService.updateMain(olds, detailLsEntity);
									updateCount++;
								}
								//更新进度
								progress=progress+1;
								//写入进度
								ProgressSingleton.put(user.getUsername()+"Progress", progress);	
				          } 
			    	 //同步完成之后，从单例中移除本次同步状态信息
				     ProgressSingleton.remove(user.getUsername()+"Size");
				     ProgressSingleton.remove(user.getUsername()+"Progress");
				     
				     resMap.put("result","1");
				     resMap.put("success","同步数据：增加数量"+addCount+"条，更新数据"+updateCount+"条。");
				  }
				return resMap;
	       }
	
	
	
	/**
	 * 同步装箱历史数据到本地
	 * @param key
	 * @param user
	 * @param queryLs
	 * @return
	 * @throws Exception
	 * @date 2017年11月23日 上午10:27:58 
	 * @author laifuwei
	 *
	 */
	public Map<String, String> findEncasemenInfo(SmtMarketUserEntity user, List<String> queryLs)
			throws Exception {
		
		Map<String,String> resMap=new HashMap<String,String>();
		int addCount=0;//添加条数
		int updateCount=0;//更新条数
		/*
		 *装箱单表头数据同步 
		 *拼接查询表头xml
		 */
		 String headXml=QueryUtil.getHeadXml(user, queryLs, "6");
    	//=================
		//===========================
			String response = this.subjectIntFaceFacade.sendDeclaration(ResourceUtil.getConfigByName("corpCode"),ResourceUtil.getConfigByName("corpName"),ResourceUtil.getConfigByName("loginCode"),ResourceUtil.getConfigByName("loginPassWord"), headXml);
			//logger.info(DateUtils.getDate("yyyy-MM-dd HH:mm:ss")+" 组货历史表头数据查询Response："+response);
			if(StringUtils.isEmpty(response)){
			    throw new Exception("市场采购贸易系统未返回回执！"); 
			}
			JSONObject HeadJsonObj =new JSONObject(response);
			//查询错误返回结果
			if(Integer.parseInt(HeadJsonObj.get("result").toString())==0){
				String message="";
				if(HeadJsonObj.has("errorMessage")){
					JSONObject dataJson=HeadJsonObj.getJSONArray("errorMessage").getJSONObject(0);
					message=dataJson.toString();					
				}else if(HeadJsonObj.has("message")){
					message=HeadJsonObj.getJSONArray("message").toString();
				}
				resMap.put("errorMessage", message);
				resMap.put("result", "0");
				logger.info(DateUtils.getDate("yyyy-MM-dd HH:mm:ss")+"装箱历史表头数据同步查询ErrorMessage："+HeadJsonObj.toString());
				return resMap;
			}else if(HeadJsonObj.has("otherMessage")){

				JSONArray HeadJsonArr = HeadJsonObj.getJSONArray("otherMessage");
				//将单例嘻哈表写入数据条数
				ProgressSingleton.put(user.getUsername()+"Size", HeadJsonArr.length());
				int progress=0;
				
			    	 for (int i = 0; i < HeadJsonArr.length(); i++) {
							JSONObject EncasemenEntityJson = (JSONObject)HeadJsonArr.get(i);
							//装箱单号
							String billno=EncasemenEntityJson.getString("billno");
							
							//存储查询装箱单表头实体信息
							SmtEncasemenEntity smtEncasemen = new SmtEncasemenEntity();
							
							if(EncasemenEntityJson.get("packDate") instanceof String||EncasemenEntityJson.get("packDate") instanceof Date){
								smtEncasemen.setCreateDate(DateUtils.str2Date(EncasemenEntityJson.get("packDate").toString(),DateUtils.date_sdf));
								smtEncasemen.setCreateTime(DateUtils.str2Date(EncasemenEntityJson.get("packDate").toString(),DateUtils.date_sdf));
							}
							if(EncasemenEntityJson.get("operateTime") instanceof String||EncasemenEntityJson.get("operateTime") instanceof Date){
								smtEncasemen.setUpdateDate(DateUtils.str2Date(EncasemenEntityJson.get("operateTime").toString(),DateUtils.datetimeFormat));
							}
							//装箱单号
							smtEncasemen.setEncaseNo(billno);
							smtEncasemen.setCompanyName(EncasemenEntityJson.getString("customCorp"));
							smtEncasemen.setCompanyId(EncasemenEntityJson.getString("operator"));
							smtEncasemen.setEncaseDate(EncasemenEntityJson.getString("packDate"));
							smtEncasemen.setWarehouseId(QueryUtil.set(EncasemenEntityJson.get("packWarehouse").toString()));
							smtEncasemen.setWarehouseName(QueryUtil.set(EncasemenEntityJson.get("packWarehouse").toString()));
							//目的地
							smtEncasemen.setDestCountryName(EncasemenEntityJson.getString("destCountryName"));
							smtEncasemen.setDestCountryId(EncasemenEntityJson.getString("destCountry"));
							smtEncasemen.setLadingNo(QueryUtil.set(EncasemenEntityJson.get("billCode").toString()));
							//报关
							smtEncasemen.setDecCustomId(EncasemenEntityJson.getString("customProxyer"));
							smtEncasemen.setDecCustomName(EncasemenEntityJson.getString("customProxyerName"));
							if(EncasemenEntityJson.getString("customStatus").equals("1")){
								smtEncasemen.setDecCustomStatus("80001");//预录入
							}else if(EncasemenEntityJson.getString("customStatus").equals("2")){
								smtEncasemen.setDecCustomStatus("80002");//已发送
							}else{
								smtEncasemen.setDecCustomStatus(EncasemenEntityJson.getString("customStatus"));
							}
							smtEncasemen.setDecCustomDate(QueryUtil.set(EncasemenEntityJson.get("customTime").toString()));
							
							//报检
							smtEncasemen.setDecQualityId(EncasemenEntityJson.getString("ciqCorp"));
							smtEncasemen.setDecQualityName(EncasemenEntityJson.getString("ciqCorpName"));
							if(EncasemenEntityJson.getString("ciqStatus").equals("1")){
								smtEncasemen.setDecQualityStatus("90001");//预录入
							}else if(EncasemenEntityJson.getString("ciqStatus").equals("2")){
								smtEncasemen.setDecQualityStatus("90002");//已发送
							}else{
								smtEncasemen.setDecQualityStatus(EncasemenEntityJson.getString("ciqStatus"));
							}
							smtEncasemen.setDecQualityDate(QueryUtil.set(EncasemenEntityJson.get("ciqTime").toString()));
							
							smtEncasemen.setStatus("60002");
							smtEncasemen.setRemark(QueryUtil.set(EncasemenEntityJson.get("remark").toString()));
							smtEncasemen.setSencaseNo(billno);
							//=============================================
							/*
							 * 查询装箱明细xml
							 */
							String detailXml=QueryUtil.getDetailXml(user, billno,"7");
							String responseDetail = this.subjectIntFaceFacade.sendDeclaration(ResourceUtil.getConfigByName("corpCode"),ResourceUtil.getConfigByName("corpName"),ResourceUtil.getConfigByName("loginCode"),ResourceUtil.getConfigByName("loginPassWord"),detailXml);
							if(StringUtils.isEmpty(responseDetail)){
							    throw new Exception("装箱单明细:市场采购贸易系统未返回回执！"); 
							}
							JSONObject detailJsonObj =new JSONObject(responseDetail);
							
							List<SmtEncasementDetailEntity> detailLsEntity=new ArrayList<SmtEncasementDetailEntity>();
							
							if(Integer.parseInt(detailJsonObj.get("result").toString())==0){
								JSONObject errJson=detailJsonObj.getJSONArray("errorMessage").getJSONObject(0);
								//resMap.put("errorDetail", errJson.toString());
								//resMap.put("result", "0");
								logger.info(DateUtils.getDate("yyyy-MM-dd HH:mm:ss")+" 装箱单历史"+billno+"明细据同步查询ErrorDetail："+errJson.toString());
								//return resMap;
							}else if(detailJsonObj.has("otherMessage")){
								//明细查询成功
								JSONArray detailJsonArr = detailJsonObj.getJSONArray("otherMessage");
							    	 for (int z= 0; z < detailJsonArr.length(); z++) {
											JSONObject details = (JSONObject)detailJsonArr.get(z);
											String groupid=details.getString("comboId");//装箱单号
											SmtEncasementDetailEntity zxDetailEntity=new SmtEncasementDetailEntity();
													
											if(EncasemenEntityJson.get("packDate") instanceof String||EncasemenEntityJson.get("packDate") instanceof Date){
												zxDetailEntity.setCreateDate(DateUtils.str2Date(EncasemenEntityJson.get("packDate").toString(),DateUtils.date_sdf));
												zxDetailEntity.setCreateTime(DateUtils.str2Date(EncasemenEntityJson.get("packDate").toString(),DateUtils.date_sdf));
											}
											if(EncasemenEntityJson.get("operateTime") instanceof String||EncasemenEntityJson.get("operateTime") instanceof Date){
												zxDetailEntity.setUpdateDate(DateUtils.str2Date(EncasemenEntityJson.get("operateTime").toString(),DateUtils.datetimeFormat));
											}
											//==detail
											zxDetailEntity.setGroupId(groupid);
											zxDetailEntity.setGroupSn(groupid);
											zxDetailEntity.setContainerName(details.getString("containerType"));
											zxDetailEntity.setContainerId(details.getString("containerType"));
											zxDetailEntity.setContainerNumber(QueryUtil.set(details.get("containerNo").toString()));
											
											detailLsEntity.add(zxDetailEntity);
										}
							}
							//=========================================
							String sql = "select id from smt_encasemen where encase_no = ? and create_by= ?";
							List<Map<String, Object>> list = this.findForJdbc(sql, new Object[]{billno,user.getUsername()});
							//System.out.println("LSsize()==="+list.size()+"==="+list);
							if(list == null || list.size() == 0){
								//添加新的装箱單
								smtEncasemenService.addMain(smtEncasemen, detailLsEntity);
								addCount++;
							}else if(list.size()>0){
								//更新旧装箱单信息
								String updateId=(String) list.get(0).get("id");
								SmtEncasemenEntity olds= smtEncasemenService.getEntity(SmtEncasemenEntity.class,updateId);
								MyBeanUtils.copyBeanNotNull2Bean(smtEncasemen,olds);
								smtEncasemenService.updateMain(olds, detailLsEntity);
								updateCount++;
							}
							//更新进度
							progress=progress+1;
							//写入进度
							ProgressSingleton.put(user.getUsername()+"Progress", progress);
			          } 
		    	//同步完成之后，从单例中移除本次同步状态信息
			     ProgressSingleton.remove(user.getUsername()+"Size");
			     ProgressSingleton.remove(user.getUsername()+"Progress");
			     resMap.put("result","1");
			     resMap.put("success","同步数据：增加数量"+addCount+"条，更新数据"+updateCount+"条。");
			  }
		return resMap;
	}

	/**
	 * 同步报关历史数据到本地
	 * @date 2017年11月24日 上午11:01:47 
	 * @author laifuwei
	 *
	 */
	public Map<String, String> findDecrationCusInfo(SmtMarketUserEntity user, List<String> queryLs) throws Exception {
		// TODO return resultMap
		
		Map<String,String> resMap=new HashMap<String,String>();
		int addCount=0;//添加条数
		int updateCount=0;//更新条数
		/*
		 *报关表头数据同步 
		 *拼接查询表头xml
		 */
		 String headXml=QueryUtil.getHeadXml(user, queryLs, "8");
		//=================
			//===========================
				String response = this.subjectIntFaceFacade.sendDeclaration(ResourceUtil.getConfigByName("corpCode"),ResourceUtil.getConfigByName("corpName"),ResourceUtil.getConfigByName("loginCode"),ResourceUtil.getConfigByName("loginPassWord"), headXml);
				//logger.info(DateUtils.getDate("yyyy-MM-dd HH:mm:ss")+" 报关历史表头数据查询Response："+response);
				if(StringUtils.isEmpty(response)){
				    throw new Exception("市场采购贸易系统未返回回执！"); 
				}
				JSONObject HeadJsonObj =new JSONObject(response);
				//查询错误返回结果
				if(Integer.parseInt(HeadJsonObj.get("result").toString())==0){
					String message="";
					if(HeadJsonObj.has("errorMessage")){
						JSONObject dataJson=HeadJsonObj.getJSONArray("errorMessage").getJSONObject(0);
						message=dataJson.toString();					
					}else if(HeadJsonObj.has("message")){
						message=HeadJsonObj.getJSONArray("message").toString();
					}
					resMap.put("errorMessage", message);
					resMap.put("result", "0");
					logger.info(DateUtils.getDate("yyyy-MM-dd HH:mm:ss")+"装箱历史数据同步查询ErrorMessage："+HeadJsonObj.toString());
					return resMap;
				}else if(HeadJsonObj.has("otherMessage")){
					JSONArray HeadJsonArr = HeadJsonObj.getJSONArray("otherMessage");
					//将单例嘻哈表写入数据条数
					ProgressSingleton.put(user.getUsername()+"Size", HeadJsonArr.length());
					int progress=0;
					
			    	 for (int i = 0; i < HeadJsonArr.length(); i++) {
							JSONObject DecrationCusEntityJson = (JSONObject)HeadJsonArr.get(i);
							//单号
							String decNo=QueryUtil.set(DecrationCusEntityJson.getString("ENTRY_ID"));
							
							//存储查询信息
							SmtDecrationCusEntity smtDecrationCus = new SmtDecrationCusEntity();
							
							if(DecrationCusEntityJson.get("D_DATE") instanceof String||DecrationCusEntityJson.get("D_DATE") instanceof Date){
								smtDecrationCus.setCreateDate(DateUtils.str2Date(DecrationCusEntityJson.get("D_DATE").toString(),DateUtils.date_sdf));
							}
							smtDecrationCus.setBusinessType(DecrationCusEntityJson.getString("OP_TYPE"));
							smtDecrationCus.setDecOrTransfer(DecrationCusEntityJson.getString("DEC_TIN"));
							smtDecrationCus.setDecModel(DecrationCusEntityJson.getString("DEC_MODE"));
							smtDecrationCus.setGuaranteeShall(QueryUtil.set(DecrationCusEntityJson.getString("CHECK_SURETYNAME")));
							smtDecrationCus.setListType(QueryUtil.set(DecrationCusEntityJson.getString("BILLTYPE")));
							smtDecrationCus.setUniformNo(DecrationCusEntityJson.getString("SEQ_NO_NEW"));
							smtDecrationCus.setNumber(DecrationCusEntityJson.getString("SEQ_NO_NEW"));
							smtDecrationCus.setInputNo(QueryUtil.set(DecrationCusEntityJson.getString("PRE_ENTRY_ID")));
							smtDecrationCus.setDecCode(decNo);
							smtDecrationCus.setExportPort(QueryUtil.set(DecrationCusEntityJson.getString("I_E_PORTNAME")));
							smtDecrationCus.setExportPortCode(QueryUtil.set(DecrationCusEntityJson.getString("I_E_PORT")));
							smtDecrationCus.setContractNo(QueryUtil.set(DecrationCusEntityJson.getString("CONTR_NO")));
							
							if(DecrationCusEntityJson.get("I_E_DATE") instanceof String||DecrationCusEntityJson.get("I_E_DATE") instanceof Date){
								smtDecrationCus.setExportDate(DateUtils.str2Date(DecrationCusEntityJson.get("I_E_DATE").toString(),DateUtils.date_sdf));
							}
							if(DecrationCusEntityJson.get("D_DATE") instanceof String||DecrationCusEntityJson.get("D_DATE") instanceof Date){
								smtDecrationCus.setDecDate(DateUtils.str2Date(DecrationCusEntityJson.get("D_DATE").toString(),DateUtils.date_sdf));
							}
							smtDecrationCus.setConsigner(QueryUtil.set(DecrationCusEntityJson.getString("TRADE_NAME")));
							smtDecrationCus.setComType(QueryUtil.set(DecrationCusEntityJson.getString("CO_OWNER")));
							smtDecrationCus.setBusUniformNo(QueryUtil.set(DecrationCusEntityJson.getString("TRADECOSCC")));
							smtDecrationCus.setSalesUnit(QueryUtil.set(DecrationCusEntityJson.getString("OWNER_CODE")));
							smtDecrationCus.setShipperUniformNo(QueryUtil.set(DecrationCusEntityJson.getString("OWNERCODESCC")));
							smtDecrationCus.setApplyComId(QueryUtil.set(DecrationCusEntityJson.getString("AGENTCODESCC")));
							smtDecrationCus.setApplyCom(QueryUtil.set(DecrationCusEntityJson.getString("AGENT_NAME")));
							smtDecrationCus.setApplyComNo(QueryUtil.set(DecrationCusEntityJson.getString("AGENTCODESCC")));
							smtDecrationCus.setInputComNo(QueryUtil.set(DecrationCusEntityJson.getString("COPCODESCC")));
							smtDecrationCus.setTransType(QueryUtil.set(DecrationCusEntityJson.getString("TRAF_MODE")));
							smtDecrationCus.setTransport(QueryUtil.set(DecrationCusEntityJson.getString("TRAF_MODENAME")));
							smtDecrationCus.setVoyageNo(QueryUtil.set(DecrationCusEntityJson.getString("VOYAGE_NO")));
							smtDecrationCus.setLadingNo(QueryUtil.set(DecrationCusEntityJson.getString("BILL_NO")));
							smtDecrationCus.setSupervise(QueryUtil.set(DecrationCusEntityJson.getString("ABBR_TRADENAME")));
							smtDecrationCus.setNatureShall(QueryUtil.set(DecrationCusEntityJson.getString("ABBR_CUTNAME")));
							smtDecrationCus.setTaxUnit(QueryUtil.set(DecrationCusEntityJson.getString("PAYMENT_MARK")));
							smtDecrationCus.setLicenseKey(QueryUtil.set(DecrationCusEntityJson.getString("LICENSE_NO")));
							smtDecrationCus.setTradeCountry(QueryUtil.set(DecrationCusEntityJson.getString("TRADE_AREA_CODENAME")));
							smtDecrationCus.setArriveCountry(QueryUtil.set(DecrationCusEntityJson.getString("TRADE_COUNTRYNAME")));
							smtDecrationCus.setArrivePort(QueryUtil.set(DecrationCusEntityJson.getString("PORT_NAME")));
							smtDecrationCus.setGoodsSource(QueryUtil.set(DecrationCusEntityJson.getString("DISTRICT_NAME")));
							smtDecrationCus.setApprovalNum(QueryUtil.set(DecrationCusEntityJson.getString("APPR_NO")));
							smtDecrationCus.setCommitments(QueryUtil.set(DecrationCusEntityJson.getString("PROMISEITMES")));
							smtDecrationCus.setDealWay(QueryUtil.set(DecrationCusEntityJson.getString("TRANS_MODE")));
							smtDecrationCus.setFreightItem(QueryUtil.set(DecrationCusEntityJson.getString("FEE_MARK")));
							smtDecrationCus.setFreight(QueryUtil.set(DecrationCusEntityJson.getString("FEE_RATE")));
							smtDecrationCus.setFreightCurrency(QueryUtil.getCurrency(DecrationCusEntityJson.getString("FEE_CURR")));
							smtDecrationCus.setPremiumItem(QueryUtil.set(DecrationCusEntityJson.getString("INSUR_MARK")));
							smtDecrationCus.setPremium(QueryUtil.set(DecrationCusEntityJson.getString("INSUR_RATE")));
							smtDecrationCus.setPremiumCurrency(QueryUtil.set(DecrationCusEntityJson.getString("OTHER_CURR")));
							smtDecrationCus.setFee(QueryUtil.set(DecrationCusEntityJson.getString("OTHER_RATE")));
							smtDecrationCus.setFeeCurrency(QueryUtil.set(DecrationCusEntityJson.getString("OTHER_CURR")));
							smtDecrationCus.setPieceNum(QueryUtil.set(DecrationCusEntityJson.getString("PACK_NO")));
							smtDecrationCus.setPackingType(QueryUtil.set(DecrationCusEntityJson.getString("WRAP_TYPE")));
							smtDecrationCus.setGrossWeight(QueryUtil.set(DecrationCusEntityJson.getString("GROSS_WT")));
							smtDecrationCus.setNetWeight(QueryUtil.set(DecrationCusEntityJson.getString("NET_WT")));
							smtDecrationCus.setContainerNum(QueryUtil.set(DecrationCusEntityJson.getString("JZXSL")));
							smtDecrationCus.setDecCustom(QueryUtil.set(DecrationCusEntityJson.getString("CUSTOM_MASTERNAME")));
							smtDecrationCus.setDecCustomCode(QueryUtil.set(DecrationCusEntityJson.getString("CUSTOM_MASTER")));
							smtDecrationCus.setEncaseNo(QueryUtil.set(DecrationCusEntityJson.getString("ZXD_BILLNO")));
							String status=QueryUtil.set(DecrationCusEntityJson.getString("ID_CHK"));
							if(status.equals("0")){
								status="80001";
							}else if(status.equals("1")){
								status="80002";
							}
							smtDecrationCus.setStatus(status);
							smtDecrationCus.setRelateCusDeclaration(QueryUtil.set(DecrationCusEntityJson.getString("RELATIVE_ID")));
							smtDecrationCus.setRelateRecord(QueryUtil.set(DecrationCusEntityJson.getString("RELATIVE_MANUAL_NO")));
							smtDecrationCus.setBondedPlace(QueryUtil.set(DecrationCusEntityJson.getString("BONDED_NO")));
							smtDecrationCus.setStorageAreaCode(QueryUtil.set(DecrationCusEntityJson.getString("CUSTOMS_FIELD")));
							smtDecrationCus.setOperator(QueryUtil.set(DecrationCusEntityJson.getString("TYPIST_NO")));
							smtDecrationCus.setContactWay(QueryUtil.set(DecrationCusEntityJson.getString("BP_NO")));
							smtDecrationCus.setRelateDecModel(QueryUtil.set(DecrationCusEntityJson.getString("ENTRY_TYPE")));
							smtDecrationCus.setRemark(QueryUtil.set(DecrationCusEntityJson.getString("NOTE_S")));
							smtDecrationCus.setEncaseId(QueryUtil.set(DecrationCusEntityJson.getString("ZXD_BILLNO")));
							smtDecrationCus.setOwnerCode(QueryUtil.set(DecrationCusEntityJson.getString("OWNER_CODE")));
							smtDecrationCus.setAgentCode(QueryUtil.set(DecrationCusEntityJson.getString("agentCode")));
							smtDecrationCus.setExportPortCode(QueryUtil.set(DecrationCusEntityJson.getString("I_E_PORT")));
							smtDecrationCus.setExportPort(QueryUtil.set(DecrationCusEntityJson.getString("I_E_PORTNAME")));
							smtDecrationCus.setSuperviseCode(QueryUtil.set(DecrationCusEntityJson.getString("TRADE_MODE")));
							smtDecrationCus.setNatureShallCode(QueryUtil.set(DecrationCusEntityJson.getString("CUT_MODE")));
							smtDecrationCus.setTradeCountryCode(QueryUtil.set(DecrationCusEntityJson.getString("TRADEAREACODE")));
							smtDecrationCus.setArriveCountryCode(QueryUtil.set(DecrationCusEntityJson.getString("TRADE_COUNTRY")));
							smtDecrationCus.setArrivePortCode(QueryUtil.set(DecrationCusEntityJson.getString("DISTINATE_PORT")));
							smtDecrationCus.setGoodsSourceCode(QueryUtil.set(DecrationCusEntityJson.getString("DISTRICT_CODE")));
							smtDecrationCus.setCopId(QueryUtil.set(DecrationCusEntityJson.getString("COP_ID")));
							smtDecrationCus.setNativeTrafMode(QueryUtil.set(DecrationCusEntityJson.getString("TRAF_MODE")));
							smtDecrationCus.setNativeShipName(QueryUtil.set(DecrationCusEntityJson.getString("TRAF_MODENAME")));
							smtDecrationCus.setRecordNumber(QueryUtil.set(DecrationCusEntityJson.getString("BILL_NO")));
							smtDecrationCus.setVoyageno(QueryUtil.set(DecrationCusEntityJson.getString("VOYAGE_NO")));
							smtDecrationCus.setBillNo(QueryUtil.set(DecrationCusEntityJson.getString("BILL_NO")));
							
							String sql = "select id from smt_decration_cus where number = ? and create_by= ?";
							List<Map<String, Object>> list = this.findForJdbc(sql, new Object[]{DecrationCusEntityJson.getString("SEQ_NO_NEW"),user.getUsername()});
							if(list == null || list.size() == 0){
								//添加
								this.save(smtDecrationCus);
								addCount++;
							}else if(list.size()>0){
								//更新
								String updateId=(String) list.get(0).get("id");
								SmtDecrationCusEntity olds= this.getEntity(SmtDecrationCusEntity.class,updateId);
								MyBeanUtils.copyBeanNotNull2Bean(smtDecrationCus,olds);
								this.saveOrUpdate(olds);
								updateCount++;
							}
						//更新进度
						progress=progress+1;
						//写入进度
						ProgressSingleton.put(user.getUsername()+"Progress", progress);
			    	 }
			    	 //同步完成之后，从单例中移除本次同步状态信息
				     ProgressSingleton.remove(user.getUsername()+"Size");
				     ProgressSingleton.remove(user.getUsername()+"Progress");
				     
			    	 resMap.put("result","1");
				     resMap.put("success","同步数据：增加数量"+addCount+"条，更新数据"+updateCount+"条。");
				}
		return resMap;
	}
	
	/**
	 * 查询委托代理出口协议
	 * @Title: findContract
	 * @Description: 
	 * @author wushijie 
	 * @date 2017年11月23日 上午10:57:03
	 */
	public Map<String,Object> findContract(SmtMarketUserEntity user,String orderSn) throws Exception{
		Map<String,Object> resMap=new HashMap<String,Object>();
		///int addCount=0;//添加条数
		// updateCount=0;//更新条数
		String headXml = QueryUtil.getHeadXmlByQueryType(user, orderSn,"10");
		//logger.info(DateUtils.getDate("yyyy-MM-dd HH:mm:ss")+" 委托代理出口协议查询请求："+headXml);
		String response = this.subjectIntFaceFacade.sendDeclaration(ResourceUtil.getConfigByName("corpCode"),ResourceUtil.getConfigByName("corpName"),ResourceUtil.getConfigByName("loginCode"),ResourceUtil.getConfigByName("loginPassWord"), headXml);
		//logger.info(DateUtils.getDate("yyyy-MM-dd HH:mm:ss")+" 委托代理出口协议查询结果："+response);
		if(StringUtils.isEmpty(response)){
		    throw new Exception("系统未返回回执,请联系管理员！"); 
		}
		JSONObject jsonObject =new JSONObject(response);
		JSONArray jsonArray = null;
		List<String> lsId=new ArrayList<String>();
		if((Integer)jsonObject.get("result")==1 && !jsonObject.isNull("otherMessage")){
		    jsonArray = jsonObject.getJSONArray("otherMessage");
		    if(jsonArray!=null && jsonArray.length()>0){
		    	 for (int i = 0; i < jsonArray.length(); i++) {
					JSONObject json = (JSONObject)jsonArray.get(i);
					SmtAgentContractEntity oldContract = this.findUniqueByProperty(SmtAgentContractEntity.class, "contractNum", json.getString("contractNo"));
					if(oldContract != null ){
						lsId.add(oldContract.getId());
						continue;
					}else{
						String sql="SELECT create_by AS server_id,create_name FROM smt_business WHERE reg_num = ?";
						Map<String, Object> comp = this.findOneForJdbc(sql, new Object[]{json.getString("trusteeCorpSgsCode")});
						
						SmtAgentContractEntity contract = new SmtAgentContractEntity();
						contract.setContractNum(json.getString("contractNo"));
						contract.setOrderId(json.getString("tradeBillId"));
						contract.setOrderSn(json.getString("tradeBillId"));
						if(comp==null){
							contract.setServerId(json.getString("trusteeCorpSgsCode"));
						}else{
							contract.setServerId(comp.get("server_id").toString());
							contract.setCreateBy(comp.get("server_id").toString());
							contract.setCreateName(comp.get("create_name").toString());
							contract.setCreateDate(DateUtils.parseDate(DateUtils.getDate("yyyy-MM-dd HH:mm:ss"), "yyyy-MM-dd"));
						}
						contract.setServerName(json.getString("trusteeCorpName"));
						contract.setClientId(user.getCreateBy());
						contract.setClientName(json.getString("entrustCorpName"));
						contract.setSignDate(DateUtils.parseDate(json.getString("makeDate"), "yyyy-MM-dd"));
						String stauts=json.getString("status");
						if(stauts.equals("1")){
							stauts="40001";
						}else if(stauts.equals("2")){
							stauts="40002";
						}else if(stauts.equals("3")){
							stauts="40003";
						}else if(stauts.equals("4")){
							stauts="40004";
						}else if(stauts.equals("5")){
							stauts="40005";
						}
						contract.setStatus(stauts);
						//添加新的代理出口证明
						Serializable newId=this.save(contract);
						lsId.add(newId.toString());
					 } 
		    	 }
		    }
		     resMap.put("result","1");
		     resMap.put("msg","查询成功");
		     resMap.put("returnIds", lsId);
		}else{
			String message="查询结果为空！";
			if(jsonObject.has("errorMessage")){
				JSONObject dataJson=jsonObject.getJSONArray("errorMessage").getJSONObject(0);
				message=dataJson.toString();					
			}else if(jsonObject.has("message")){
				message=jsonObject.getJSONArray("message").toString();
			}
			resMap.put("msg", message);
			resMap.put("result", "0");
			logger.info(DateUtils.getDate("yyyy-MM-dd HH:mm:ss")+"查询委托代理出口协议ErrorMessage："+jsonObject.toString());
			return resMap;
		}
		return resMap;
	}
	
	/**
	 * 查询代理出口货物证明和明细信息
	 * @Title: findGoodsCert
	 * @Description: 
	 * @author wushijie 
	 * @date 2017年11月24日 上午11:12:04
	 */
	public Map<String,Object> findGoodsCert(SmtMarketUserEntity user) throws Exception{
		Map<String,Object> resMap=new HashMap<String,Object>();
		int addCount=0;//添加条数
	//	int updateCount=0;//更新条数
		String headXml = QueryUtil.getHeadXmlByQueryType(user, "","12");
		logger.info(DateUtils.getDate("yyyy-MM-dd HH:mm:ss")+" 代理出口货物证明查询请求："+headXml);
		String response = this.subjectIntFaceFacade.sendDeclaration(ResourceUtil.getConfigByName("corpCode"),ResourceUtil.getConfigByName("corpName"),ResourceUtil.getConfigByName("loginCode"),ResourceUtil.getConfigByName("loginPassWord"), headXml);
		logger.info(DateUtils.getDate("yyyy-MM-dd HH:mm:ss")+" 代理出口货物证明查询结果："+response);
		if(StringUtils.isEmpty(response)){
		    resMap.put("msg", "系统未返回回执,请联系管理员！");
			resMap.put("status", "false");
			return resMap;
		}
		JSONObject jsonObject =new JSONObject(response);
		JSONArray jsonArray = null;
		List<String> ls=new ArrayList<String>();
		if((Integer)jsonObject.get("result")==1 && !jsonObject.isNull("otherMessage")){
		    jsonArray = jsonObject.getJSONArray("otherMessage");
		    if(jsonArray!=null && jsonArray.length()>0){
		    	 for (int i = 0; i < jsonArray.length(); i++) {
		    		 JSONObject json = (JSONObject)jsonArray.get(i);
		    			 SmtProveEntity prove = new SmtProveEntity();
		    			 prove.setCreateBy(user.getCreateBy());
		    			 prove.setCreateName(user.getCreateName());
		    			 prove.setCreateDate(DateUtils.parseDate(DateUtils.getDate("yyyy-MM-dd HH:mm:ss"), "yyyy-MM-dd HH:mm:ss"));
		    			 prove.setRecNo(json.getString("recNo"));
		    			 prove.setEntrustCorpSgsCode(json.getString("entrustCorpSgsCode"));
		    			 prove.setEntrustCorpName(json.getString("entrustCorpName"));
		    			 prove.setEntrustCorpTax(json.getString("entrustCorpTax"));
		    			 prove.setTrusteeCorpSgsCode(json.getString("trusteeCorpSgsCode"));
		    			 prove.setTrusteeCorpName(json.getString("trusteeCorpName"));
		    			 prove.setTrusteeCorpCode(json.getString("trusteeCorpCode"));
		    			 prove.setOperateTime(DateUtils.parseDate(json.getString("operateTime"), "yyyy-MM-dd"));
		    			 prove.setIssueDate(DateUtils.parseDate(json.getString("issueDate"), "yyyy-MM-dd"));
		    			 prove.setOperator(json.getString("operator"));
		    			 prove.setStatus(json.getString("status"));
		    			 
		    			 String detailXml = QueryUtil.getDetailXml(user, json.getString("recNo"), "13");
		    			 String detailResponse = this.subjectIntFaceFacade.sendDeclaration(ResourceUtil.getConfigByName("corpCode"),ResourceUtil.getConfigByName("corpName"),ResourceUtil.getConfigByName("loginCode"),ResourceUtil.getConfigByName("loginPassWord"), detailXml);
		    			 //logger.info(DateUtils.getDate("yyyy-MM-dd HH:mm:ss")+" 代理出口货物证明明细查询结果："+detailResponse);
		    			 if(StringUtils.isEmpty(detailResponse)){
		    				    //throw new Exception("系统未返回回执,请联系管理员！"); 
		    				    continue;
		    			 }
		    			 JSONObject detailJsonObject =new JSONObject(detailResponse);
		    			 JSONArray detailJsonArray = null;
		    			 
		    			 List<SmtProveDetailEntity> detailList = new ArrayList<SmtProveDetailEntity>();
		    			 if((Integer)detailJsonObject.get("result")==1 && !detailJsonObject.isNull("otherMessage")){
		    				 detailJsonArray = detailJsonObject.getJSONArray("otherMessage");
		    				 if(detailJsonArray!=null && detailJsonArray.length()>0){
		    					 for (int j = 0; j < detailJsonArray.length(); j++) {
		    						 JSONObject detailJson = (JSONObject)detailJsonArray.get(j);
		    						 SmtProveDetailEntity detail = new SmtProveDetailEntity();
		    						 detail.setCreateBy(user.getCreateBy());
		    						 detail.setCreateName(user.getCreateName());
		    						 detail.setCreateDate(DateUtils.parseDate(DateUtils.getDate("yyyy-MM-dd HH:mm:ss"), "yyyy-MM-dd HH:mm:ss"));
		    						 detail.setGoodsCertNo(detailJson.getString("goodsCertNo"));
		    						 detail.setCustomNo(detailJson.getString("customNo"));
		    						 detail.setExportDate(DateUtils.parseDate(detailJson.getString("exportDate"), "yyyy-MM-dd"));
		    						 detail.setTradeType(detailJson.getString("tradeType"));
		    						 detail.setGoodsCode(detailJson.getString("goodsCode"));
		    						 detail.setGoodsName(detailJson.getString("goodsName"));
		    						 detail.setModel(detailJson.getString("model"));
		    						 detail.setUnit(detailJson.getString("unit"));
		    						 detail.setQuantity(detailJson.getString("quantity"));
		    						 detail.setCcy(detailJson.getString("ccy"));
		    						 detail.setAmount(detailJson.getString("amount"));
		    						 detail.setContractNum(detailJson.getString("entrustAgentConId"));
		    						 detailList.add(detail);
		    				    }
		    				}
		    				String sql = "select id from smt_prove where rec_no = ?";
							List<Map<String, Object>> list = this.findForJdbc(sql, new Object[]{json.getString("recNo")}); 
							if(list == null || list.size() == 0){
								//添加新的代理出口货物证明
								smtProveService.addMain(prove, detailList);
								ls.add(prove.getId());
								addCount++;
							}else if(list.size()>0){
								//更新旧得代理出口货物证明
								String updateId=(String) list.get(0).get("id");
								ls.add(updateId);
								/*SmtProveEntity olds= smtProveService.getEntity(SmtProveEntity.class,updateId);
								MyBeanUtils.copyBeanNotNull2Bean(prove,olds);
								smtProveService.updateMain(olds, detailList);
								updateCount++;*/
							} 
		    			}
		    	 }
		    }	 
		    resMap.put("status","true");
			resMap.put("ids", ls);
		    resMap.put("msg","查询数据：增加数量"+addCount+"条");
		}else{
			resMap.put("status","false");
			resMap.put("msg","查询失败");
		}
	    return resMap;
	}
	
	/**
	 *  免税申报 查询
	 * @Title: findFreetax
	 * @Description: 
	 * @author wushijie 
	 * @date 2017年11月24日 上午11:12:04
	 */
	public Map<String,Object> findFreetax(SmtMarketUserEntity user) throws Exception{
		Map<String,Object> resMap=new HashMap<String,Object>();
		int addCount=0;//添加条数
		//int updateCount=0;//更新条数
		String headXml = QueryUtil.getHeadXmlByQueryType(user,"", "14");
		logger.info(DateUtils.getDate("yyyy-MM-dd HH:mm:ss")+"  免税申报查询请求："+headXml);
		String response = this.subjectIntFaceFacade.sendDeclaration(ResourceUtil.getConfigByName("corpCode"),ResourceUtil.getConfigByName("corpName"),ResourceUtil.getConfigByName("loginCode"),ResourceUtil.getConfigByName("loginPassWord"), headXml);
		logger.info(DateUtils.getDate("yyyy-MM-dd HH:mm:ss")+"  免税申报查询结果："+response);
		if(StringUtils.isEmpty(response)){
		  //  throw new Exception("系统未返回回执,请联系管理员！");
		    resMap.put("msg", "系统未返回回执,请联系管理员！");
			resMap.put("status", "false");
			return resMap;
		}
		JSONObject HeadJsonObj =new JSONObject(response);
		List<String> ls=new ArrayList<String>();
		//查询错误返回结果
		if(Integer.parseInt(HeadJsonObj.get("result").toString())==0){
			String message="";
			if(HeadJsonObj.has("errorMessage")){
				JSONObject dataJson=HeadJsonObj.getJSONArray("errorMessage").getJSONObject(0);
				message=dataJson.toString();					
			}else if(HeadJsonObj.has("message")){
				message=HeadJsonObj.getJSONArray("message").toString();
			}
			resMap.put("msg", message);
			resMap.put("status", "false");
		}else if(HeadJsonObj.has("otherMessage")){
			JSONArray HeadJsonArr = HeadJsonObj.getJSONArray("otherMessage");
	    	 for (int i = 0; i < HeadJsonArr.length(); i++) {
	    		 JSONObject smtFreetaxEntityJson = (JSONObject)HeadJsonArr.get(i);
					//免税申报采购网返回id
					String freeTaxId=smtFreetaxEntityJson.getString("id");
					
					//存储查询表头实体信息
					SmtFreetaxEntity smtFreetax = new SmtFreetaxEntity();
					
					if(smtFreetaxEntityJson.get("applyDate") instanceof String||smtFreetaxEntityJson.get("applyDate") instanceof Date){
						smtFreetax.setCreateDate(DateUtils.str2Date(smtFreetaxEntityJson.get("applyDate").toString(),DateUtils.date_sdf));
					}
					smtFreetax.setCreateBy(user.getCreateBy());
					smtFreetax.setCreateName(user.getCreateName());
					smtFreetax.setNumber(freeTaxId);//
					smtFreetax.setTaxIdentCode(QueryUtil.set(smtFreetaxEntityJson.getString("taxIdentCode")));
					smtFreetax.setCorpName(QueryUtil.set(smtFreetaxEntityJson.getString("corpName")));
					smtFreetax.setTradeTaxIdentCode(QueryUtil.set(smtFreetaxEntityJson.getString("tradeTaxIdentCode")));
					if(smtFreetaxEntityJson.get("operateTime") instanceof String||smtFreetaxEntityJson.get("operateTime") instanceof Date){
						smtFreetax.setOperateTime(DateUtils.str2Date(smtFreetaxEntityJson.get("operateTime").toString(),DateUtils.date_sdf));
					}
					smtFreetax.setStatus(QueryUtil.set(smtFreetaxEntityJson.getString("status")));
					smtFreetax.setTradeCustomNo(QueryUtil.set(smtFreetaxEntityJson.getString("trade_custom_no")));
					smtFreetax.setOperator(QueryUtil.set(smtFreetaxEntityJson.getString("operator")));
					smtFreetax.setTradeCorpName(QueryUtil.set(smtFreetaxEntityJson.getString("trade_corp_name")));
					if(smtFreetaxEntityJson.get("applyDate") instanceof String||smtFreetaxEntityJson.get("applyDate") instanceof Date){
						smtFreetax.setApplyDate(DateUtils.str2Date(smtFreetaxEntityJson.get("applyDate").toString(),DateUtils.date_sdf));
					}
					//=============================================
					/*
					 * 
					 */
					String detailXml=QueryUtil.getDetailXml(user, freeTaxId,"15");
					String responseDetail = this.subjectIntFaceFacade.sendDeclaration(ResourceUtil.getConfigByName("corpCode"),ResourceUtil.getConfigByName("corpName"),ResourceUtil.getConfigByName("loginCode"),ResourceUtil.getConfigByName("loginPassWord"),detailXml);
					//logger.info(DateUtils.getDate("yyyy-MM-dd HH:mm:ss")+" 交易单"+orderNo+"明细数据查询Response："+responseDetail);
					if(StringUtils.isEmpty(responseDetail)){
    				    //throw new Exception("系统未返回回执,请联系管理员！"); 
    				    continue;
    			     }
					JSONObject detailJsonObj =new JSONObject(responseDetail);
					List<SmtFreetaxDetailEntity> detailLsEntity=new ArrayList<SmtFreetaxDetailEntity>();
					
					if(Integer.parseInt(detailJsonObj.get("result").toString())==1){
						//明细查询成功
						JSONArray detailJsonArr = detailJsonObj.getJSONArray("otherMessage");
					     for (int z= 0; z < detailJsonArr.length(); z++) {
					    		 JSONObject details = (JSONObject)detailJsonArr.get(z);
					    		 SmtFreetaxDetailEntity freeDetailEntity=new SmtFreetaxDetailEntity();
					    		 freeDetailEntity.setCreateBy(user.getCreateBy());
					    		 freeDetailEntity.setCreateName(user.getCreateName());
					    		 freeDetailEntity.setUnit(QueryUtil.set(details.getString("unit")));
					    		 if(details.get("amountOfRmb") instanceof Integer||details.get("amountOfRmb") instanceof Double){
					    			 freeDetailEntity.setAmountOfRmb(new BigDecimal(details.get("amountOfRmb").toString())); 
					    		 }
					    		 if(details.get("amountOfDollar") instanceof Integer||details.get("amountOfDollar") instanceof Double){
					    			 freeDetailEntity.setAmountOfDollar(new BigDecimal(details.get("amountOfDollar").toString())); 
					    		 }
					    		 if(details.get("exportDate") instanceof String||details.get("exportDate") instanceof Date){
					    			 freeDetailEntity.setExportDate(DateUtils.str2Date(details.get("exportDate").toString(),DateUtils.date_sdf));
								 }
					    		 freeDetailEntity.setCustomNo(QueryUtil.set(details.getString("customNo")));
					    		 freeDetailEntity.setQuantity(QueryUtil.set(details.get("quantity").toString()));
					    		 freeDetailEntity.setGoodsCode(QueryUtil.set(details.getString("goodsCode")));
					    		 freeDetailEntity.setGoodsName(QueryUtil.set(details.getString("goodsName")));
					    		 freeDetailEntity.setAgentGoodsCertId(QueryUtil.set(details.getString("agentGoodsCertId")));
					    		 freeDetailEntity.setEntrustContractId(QueryUtil.set(details.getString("entrustContractId")));
					    		 detailLsEntity.add(freeDetailEntity);
					    	 }
					    	//=========================================
							String sql = "select id from smt_freetax where number = ?";
							List<Map<String, Object>> list = this.findForJdbc(sql, new Object[]{freeTaxId});
							if(list == null || list.size() == 0){
								smtFreetaxService.addMain(smtFreetax, detailLsEntity);
								addCount++;
								ls.add(smtFreetax.getId());
							}else if(list.size()>0){
								String updateId=(String) list.get(0).get("id");
								ls.add(updateId);
							/*	SmtFreetaxEntity olds= smtFreetaxService.getEntity(SmtFreetaxEntity.class,updateId);
								MyBeanUtils.copyBeanNotNull2Bean(smtFreetax,olds);
								smtFreetaxService.updateMain(olds, detailLsEntity);
								updateCount++;*/
							} 	 
					}
	    	  }
	    	 resMap.put("status","true");
	    	 resMap.put("ids", ls);
			 resMap.put("msg","同步数据：增加数量"+addCount+"条");
		}
		logger.info(DateUtils.getDate("yyyy-MM-dd HH:mm:ss")+"免税申报历史数据查询："+resMap.toString());
		return resMap;
	}

	/**
	 * 同步报检单历史数据信息
	 * @param user
	 * @param queryLs
	 * @return
	 * @throws Exception
	 * @date 2017年12月5日 下午2:44:47 
	 * @author laifuwei
	 *
	 */
	public Map<String, Object> findDecrationCiqInfo(SmtMarketUserEntity user, List<String> queryLs) throws Exception {

		Map<String,Object> resMap=new HashMap<String,Object>();
		int addCount=0;//添加条数
		int updateCount=0;//更新条数
		/*
		 *表头数据同步 
		 *拼接查询表头xml
		 */
		 String headXml=QueryUtil.getHeadXml(user, queryLs, "9");
		//=================
			//===========================
				String response = this.subjectIntFaceFacade.sendDeclaration(ResourceUtil.getConfigByName("corpCode"),ResourceUtil.getConfigByName("corpName"),ResourceUtil.getConfigByName("loginCode"),ResourceUtil.getConfigByName("loginPassWord"), headXml);
				//logger.info(DateUtils.getDate("yyyy-MM-dd HH:mm:ss")+"报检历史表头数据查询Response："+response);
				if(StringUtils.isEmpty(response)){
				    throw new Exception("市场采购贸易系统未返回回执！"); 
				}
				JSONObject HeadJsonObj =new JSONObject(response);
				//查询错误返回结果
				if(Integer.parseInt(HeadJsonObj.get("result").toString())==0){
					String message="";
					if(HeadJsonObj.has("errorMessage")){
						JSONObject dataJson=HeadJsonObj.getJSONArray("errorMessage").getJSONObject(0);
						message=dataJson.toString();					
					}else if(HeadJsonObj.has("message")){
						message=HeadJsonObj.getJSONArray("message").toString();
					}
					resMap.put("msg", message);
					resMap.put("status", "false");
					logger.info(DateUtils.getDate("yyyy-MM-dd HH:mm:ss")+"报检历史数据同步查询ErrorMessage："+HeadJsonObj.toString());
					return resMap;
				}else if(HeadJsonObj.has("otherMessage")){
					JSONArray HeadJsonArr = HeadJsonObj.getJSONArray("otherMessage");
					//将单例嘻哈表写入数据条数
					ProgressSingleton.put(user.getUsername()+"Size", HeadJsonArr.length());
					int progress=0;
			    	 for (int i = 0; i < HeadJsonArr.length(); i++) {
			    			JSONObject DecrationCiqEntityJson = (JSONObject)HeadJsonArr.get(i);
							//报检单号
							String relaid=QueryUtil.set(DecrationCiqEntityJson.getString("relaid"));
							//装箱单号
							String billNo=QueryUtil.set(DecrationCiqEntityJson.getString("billNo"));
							//存储查询信息
							SmtDecrationCiqEntity decrationCiq = new SmtDecrationCiqEntity();
							
							if(DecrationCiqEntityJson.get("declDate") instanceof String||DecrationCiqEntityJson.get("declDate") instanceof Date){
								decrationCiq.setCreateDate(DateUtils.str2Date(DecrationCiqEntityJson.get("declDate").toString(),DateUtils.date_sdf));
							}
							decrationCiq.setCreateBy(user.getCreateBy());
							decrationCiq.setCreateName(user.getCreateName());
							decrationCiq.setRelaid(relaid);
							decrationCiq.setPackingNo(billNo);
							decrationCiq.setDecl(QueryUtil.set(DecrationCiqEntityJson.getString("DECLBCODE_NAME")));
							decrationCiq.setDeclBcode(QueryUtil.set(DecrationCiqEntityJson.getString("declbcode")));
							
							decrationCiq.setCiq(QueryUtil.set(DecrationCiqEntityJson.getString("CIQBCODE_NAME")));
							decrationCiq.setCiqBcode(QueryUtil.set(DecrationCiqEntityJson.getString("ciqbcode")));
							
							decrationCiq.setPortCiq(QueryUtil.set(DecrationCiqEntityJson.getString("PORTCIQBCODE_NAME")));
							decrationCiq.setPortCiqBcode(QueryUtil.set(DecrationCiqEntityJson.getString("portciqbcode")));
					
							decrationCiq.setTestTube(QueryUtil.set(DecrationCiqEntityJson.getString("STATIONBCODE_NAME")));
							decrationCiq.setTestTubeCode(QueryUtil.set(DecrationCiqEntityJson.getString("stationbcode")));
							
							decrationCiq.setPayer(QueryUtil.set(DecrationCiqEntityJson.getString("payercode")));
							decrationCiq.setPayerCode(QueryUtil.set(DecrationCiqEntityJson.getString("payercode")));
							decrationCiq.setOperType(QueryUtil.set(DecrationCiqEntityJson.getString("opertype")));
							decrationCiq.setProcurePlace(QueryUtil.set(DecrationCiqEntityJson.getString("procureplace")));
							decrationCiq.setLoadPlace(QueryUtil.set(DecrationCiqEntityJson.getString("loadplace")));
							decrationCiq.setShipper(QueryUtil.set(DecrationCiqEntityJson.getString("shippercode")));
							decrationCiq.setShipperCode(QueryUtil.set(DecrationCiqEntityJson.getString("shippercode")));
							decrationCiq.setCompanyCode(QueryUtil.set(DecrationCiqEntityJson.getString("companycode")));
							if(DecrationCiqEntityJson.get("planoutdate") instanceof String||DecrationCiqEntityJson.get("planoutdate") instanceof Date){
								decrationCiq.setPlanOutDate(DateUtils.str2Date(DecrationCiqEntityJson.get("planoutdate").toString(),DateUtils.date_sdf));
							}
							decrationCiq.setTradeType(QueryUtil.set(DecrationCiqEntityJson.getString("TRADETYPE_NAME")));
							decrationCiq.setTradeTypeCode(QueryUtil.set(DecrationCiqEntityJson.getString("tradetype")));
							decrationCiq.setConsignee(QueryUtil.set(DecrationCiqEntityJson.getString("consignee")));
							decrationCiq.setConsigneeAdr(QueryUtil.set(DecrationCiqEntityJson.getString("consigneeadr")));
							decrationCiq.setBargainNo(QueryUtil.set(DecrationCiqEntityJson.getString("bargainno")));
							decrationCiq.setPortLoad(QueryUtil.set(DecrationCiqEntityJson.getString("PORTLOAD_NAME")));
							decrationCiq.setPortLoadCode(QueryUtil.set(DecrationCiqEntityJson.getString("portload")));
							decrationCiq.setPortDisCode(QueryUtil.set(DecrationCiqEntityJson.getString("portdis")));
							decrationCiq.setPortDis(QueryUtil.set(DecrationCiqEntityJson.getString("PORTDIS_NAME")));
							decrationCiq.setGoodsName(QueryUtil.set(DecrationCiqEntityJson.getString("goodsname")));
							decrationCiq.setGoodType(QueryUtil.set(DecrationCiqEntityJson.getString("goodsType")));
							decrationCiq.setCurrencyCode(QueryUtil.set(DecrationCiqEntityJson.getString("fcode")));
							decrationCiq.setCurrency(QueryUtil.set(DecrationCiqEntityJson.getString("FCODE_NAME")));
							decrationCiq.setPackType(QueryUtil.set(DecrationCiqEntityJson.getString("PACKTYPE_NAME")));
							decrationCiq.setPackTypeCode(QueryUtil.set(DecrationCiqEntityJson.getString("packtype")));
							decrationCiq.setInvoiceNo(QueryUtil.set(DecrationCiqEntityJson.getString("invoiceno")));
							decrationCiq.setCousType(QueryUtil.set(DecrationCiqEntityJson.getString("coustype")));
							decrationCiq.setBlno(QueryUtil.set(DecrationCiqEntityJson.getString("blno")));
							decrationCiq.setTransType(QueryUtil.getTransType(DecrationCiqEntityJson.get("transportMode").toString()));
							decrationCiq.setTransName(QueryUtil.set(DecrationCiqEntityJson.getString("vesselcn")));
							decrationCiq.setVoyage(QueryUtil.set(DecrationCiqEntityJson.getString("voyage")));
							
						//decrationCiq.setPortStationBcode(QueryUtil.set(DecrationCiqEntityJson.getString("stationbcode")));
							decrationCiq.setConOper(QueryUtil.set(DecrationCiqEntityJson.getString("conoper")));
							decrationCiq.setManiNo(QueryUtil.set(DecrationCiqEntityJson.getString("manino")));
							decrationCiq.setBentryFlags(QueryUtil.set(DecrationCiqEntityJson.getString("bentryflags")));
							decrationCiq.setWorkMode(QueryUtil.set(DecrationCiqEntityJson.getString("workmode")));
							
							decrationCiq.setPrepare(QueryUtil.set(DecrationCiqEntityJson.getString("vprepare")));
							decrationCiq.setPrepareName(QueryUtil.set(DecrationCiqEntityJson.getString("vpreparename")));
							decrationCiq.setPayType(QueryUtil.set(DecrationCiqEntityJson.getString("paytype")));
							
							decrationCiq.setPlatFormCode(QueryUtil.set(DecrationCiqEntityJson.getString("platformcode")));
							if(DecrationCiqEntityJson.get("sumPrice") instanceof Double||DecrationCiqEntityJson.get("sumPrice") instanceof Integer){
								decrationCiq.setTotalValue(DecrationCiqEntityJson.getDouble("sumPrice"));
							}
							decrationCiq.setPayCondition(QueryUtil.set(DecrationCiqEntityJson.getString("paycondition")));
							
							decrationCiq.setRemark(QueryUtil.set(DecrationCiqEntityJson.getString("remark")));
							if(DecrationCiqEntityJson.get("declDate") instanceof String||DecrationCiqEntityJson.get("declDate") instanceof Date){
								decrationCiq.setDecDate(DateUtils.str2Date(DecrationCiqEntityJson.get("declDate").toString(),DateUtils.date_sdf));
							}
							decrationCiq.setStatus(QueryUtil.getdeclCiqStatus(DecrationCiqEntityJson.get("declFlag").toString()));
							decrationCiq.setPackingId(billNo);
							
							//smtDecrationCiqService
							String sql = "select id from smt_decration_ciq where relaid = ?";
							List<Map<String, Object>> list = this.findForJdbc(sql, new Object[]{relaid}); 
							if(list == null || list.size() == 0){
								//添加
								smtDecrationCiqService.save(decrationCiq);;
								addCount++;
							}else if(list.size()>0){
								//更新
								String updateId=(String) list.get(0).get("id");
								SmtDecrationCiqEntity olds= smtDecrationCiqService.getEntity(SmtDecrationCiqEntity.class,updateId);
								MyBeanUtils.copyBeanNotNull2Bean(decrationCiq,olds);
								smtDecrationCiqService.saveOrUpdate(olds);
								updateCount++;
							} 
						//更新进度
						progress=progress+1;
						//写入进度
						ProgressSingleton.put(user.getUsername()+"Progress", progress);
			    	 }
			    	//同步完成之后，从单例中移除本次同步状态信息
				     ProgressSingleton.remove(user.getUsername()+"Size");
				     ProgressSingleton.remove(user.getUsername()+"Progress");
				     
			    	 resMap.put("status","true");
					 resMap.put("msg","同步数据：增加数量"+addCount+"条，更新数据"+updateCount+"条。");
				}
			  return resMap;
	     }
	
	
	
}
package com.kbcard.ubd.pbi.card;

import java.math.BigDecimal;
import java.math.RoundingMode;

import com.kbcard.ubd.UBDcommon.MainCommon;
import com.kbcard.ubd.cpbi.cmn.UBD_CONST;
import com.kbcard.ubd.cpbi.cmn.UbdCdcSptFntCpbc;
import com.kbcard.ubd.cpbi.cmn.UbdCommon;
import com.kbcard.ubd.cpbi.cmn.UbdDmdRspPhsLdinCpbc;

import devon.core.collection.LData;
import devon.core.collection.LMultiData;
import devon.core.context.ContextHandler;
import devon.core.context.ContextKey;
import devon.core.exception.LException;
import devon.core.log.LLog;
import devon.core.util.LNullUtils;
import devonenterprise.business.bm.command.BizCommand;
import devonenterprise.channel.message.header.SystemHeaderManager;
import devonenterprise.ext.channel.telegram.KBCDSYSTEM_HEADERFIELD;
import devonenterprise.ext.core.context.ExtensionContextKey.LContextKey;
import devonenterprise.ext.core.exception.LBizException;
import devonenterprise.ext.core.exception.LNotFoundException;
import devonenterprise.ext.persistent.page.PageConstants;
import devonenterprise.ext.persistent.page.ScrollPageData;
import devonenterprise.ext.service.cache.service.ErrorCodeMessageService;
import devonenterprise.ext.util.ContextUtil;
import devonenterprise.ext.util.DataConvertUtil;
import devonenterprise.ext.util.FormatUtil;
import devonenterprise.ext.util.LDataUtil;
import devonenterprise.ext.util.TypeConvertUtil;
import devonenterprise.util.DateUtil;
import devonenterprise.util.StringUtil;

public class MyDtVovsAthHisPbc {

	/** 
//Ali Code For : ACSD_마이데이터해외승인내역조회 - 출력폼 셋팅 - 해외	/** 
	 *   ※ 해외승인내역조회 INPUT	   
	 *      H) Authorization      aNS(900)
	 *      H) x-api-tran-id       AN(20)
	 *      1) 기관코드			   aN(10)
	 *      2) 시작일자     	    N(8)
	 *      3) 종료일자				N(8)
	 *      4) 다음페이지기준개체   	   aN(1000)
	 *      5) 최대조회갯수 		N(3)
	 *      해외승인내역조회 OUTPUT
	 *      H) Authorization      aNS(900)
	 *      1) 세부 응답코드	   aN(5)
	 *      2) 세부 응답메시지     AH(450)
	 *      3) 다음페이지기준개체           N(1000)
	 *      4) 해외승인목록수           N(3)
	 *      5) 해외내승인목록         Object
	 *         - 승인번호          aN(64)
     *         - 승인일시          DTIME
	 *         - 결제상태          NS(19)
	 *         - 사용구분          aN(2)
	 *         - 정정또는승인취소일시   DTIME
	 *         - 가맹점명           AH(75)
	 *         - 이용금액			F(18,3)
	 *         - 정정후금액			F(18,3)
	 *         - 결제국가코드       A(2)
	 *         - 결제시통화코드		A(3)
	 */
	
	/**
	 * @serviceID 
	 * @logicalName 
	 * @param 
	 * @return 
	 * @exception 
	 */
	LData rMyDtApiTlgOut = new LData(); //금보에 회신하는 최종 데이터
	//LData iMyDtApiTlgIn = new LData();  //금보에서 받은 input 
	LData iMciInput = new LData(); //MCI거래 호출 INPUT 
	LData rMciInput = new LData(); 	//MCI거래 호출 OUTPUT
	
	LData iMciInput2 = new LData();
	LData rMciInput2		 = new LData();
	
	/**
	 * 마이데이터거래고유번호,마이데이터이용기관코드,고객식별자 , 고객관리번호 , API구분코드 , 인터페이스ID 
	 */
	LData lEncInf = new LData();
	
	
	public LData retvMyDtVovsAthHis(LData iMyDtApiTlgIn) throws LException {
		MainCommon 	mainCommon  = new MainCommon();	 //MainCommon공통 호출
		
		UbdDmdRspPhsLdinCpbc dmdRspPhsLdinCpbc = new UbdDmdRspPhsLdinCpbc();
		UbdCommon ubdCommon = new UbdCommon();
		LData header_info = new LData(); // 헤더정보
		//CdcSptFntCpbc cdcSptFntCpbc = new CdcSptFntCpbc(); //중복거래여부체크
		
		LLog.debug.println( "##############################################" );
		LLog.debug.println( "[마이데이터해외승인내역조회] PBI START #####" );
		LLog.debug.println( "[API구분코드 = " + UBD_CONST.API_DTCD_BIL_INF_INQ + " ]" );
		
		LLog.debug.println( "GUID      : [" + ContextHandler.getContextObject(ContextKey.GUID		) + "]");
		LLog.debug.println( "SITE_CODE : [" + ContextHandler.getContextObject(ContextKey.SITE_CODE	) + "]");
		LLog.debug.println( "TRAN_ID   : [" + ContextHandler.getContextObject(ContextKey.TRAN_ID	) + "]");
		
		LLog.debug.println( "[iMyDtApiTlgIn]" + iMyDtApiTlgIn );	
		
		String rErrCode = "";
		String rErrMsg  = "";
		
		int   itRowCnt		= 0;											// 데이터조회건수
		String sErrCode		= UBD_CONST.REP_CD_SUCCESS;						// 에러코드(00000)
		String sErrMsg		= UBD_CONST.REP_CD_MSG_SUCCESS;					// 에러메시지(응답내역)
		String sErrCodePrc	= ""; // 에러코드(처리계)
		String sErrMsgPrc	= ""; // 에러메시지(처리계메시지)
		
		String sCICtt		= "";	// CI내용
		String sCstIdf 		= "";	// 고객식별자
		String sCstMgNo 	= "";	// 고객관리번호	
		String sMmsn		= "";   // 회원일련번호
		String sRtvlTrsYN   = "1"; // 정기적전송여부(1:정기적전송-"x-api-type: scheduled", 2:비정기적전송(정보주체 개입)-생략)
	
		String sJbStYm = ""; // 작업기준년월일
		sJbStYm = DateUtil.getCurrentDate(); 
		
		String sixMonthago  = ""; // 6개월 이전 구분 기준이 되는 날짜
		
		LData iretvLstMydtAthHis = new LData(); // 승인내역 6개월 이전 조회 입력
		LMultiData rretvLstMydtAthHis = new LMultiData(); // 승인내역 6개월 이전 조회 결과
		
		LData iretvLstMydtPstAthHis = new LData(); // 승인내역 6개월 이후 조회 입력
		LMultiData rretvLstMydtPstAthHis = new LMultiData(); // 승인내역 6개월 이후 조회 결과

		LData iretvGagAltrCrdDtlByCrdAltrNo = new LData(); // 카드대체번호 -> 카드식별자의 input
		LData rretvGagAltrCrdDtlByCrdAltrNo = new LData(); // 카드대체번호 -> 카드식별자의 output
		
		LData ietvGagCrdDtl = new LData(); // 카드상세조회 입력
		LData retvGagCrdDtl = new LData(); // 카드상세조회 출력
		
		UbdDmdRspPhsLdinCpbc AsyncRunner = new UbdDmdRspPhsLdinCpbc();
		
		String inputnext = iMyDtApiTlgIn.getString("다음페이지기준개체"); // 고객에게 입력받은 다음페이지 기준개체
		String inputnext2 ; // 처리계에서 입력받은 다음페이지 기준개체
		String nextkeyme = ""; // 처리계로 보내는 다음페이지 조회키
		
		LData iCrdCtgInqIn = new LData();
		LMultiData rCrdCtgInqOut = new LMultiData();
		
		String sGwGuid 				= ContextHandler.getContextObject(ContextKey.GUID	);	// 게이트웨이거래식별번호
		String sTranId				= ContextHandler.getContextObject(ContextKey.TRAN_ID);	// 거래코드
		String sDmdCtt 				= ""; 					// 압축 및 암호화
		String sDmdCttCmpsEcy 		= ""; 					// 압축 및 암호화 모듈 호출
		String sPrtlDcCd 			= new String();				// 포탈분기구분코드 (HDR:금융결제원,POR:포탈)
		String sCdcMciGb			= "CDC"; 				// 처리계시스템구분(CDC, MCI, EAI)
		String strDtcd              = new String();
		String sInnMciLinkIntfId 	= "UBD_1_GBHS00003"; 	// MCI LINK 인터페이스ID
				
		String sdtErrMsgCd 			= ""; // 오류메시지코드
		String sdtLnkdOgtnGidNo 	= ""; // 연계원거래 GUID 
		String sdtGidNo 			= ""; // 거래 GUID
		String sEmpNo				= ""; // 사용자ID
		
		LData tCustInf 				= new LData(); // 고객정보
		String sAccsTken			= ""; // 접근토큰
		String sMydtTrUno			= ""; // 마이데이터거래고유번호
		String sMydtUtzInsCd 		= ""; // 마이데이터이용기관코드
		String sMydtClintIdiNo ="";
		
		LData linkRequestHeader 	= new LData();		
		LData linkResponseHeader 	= new LData();
		
		try {

		// =============================================================================
		// ######### ##마이데이터 API 요청데이터 수신
		// =============================================================================
		
			rMyDtApiTlgOut = iMyDtApiTlgIn;	// 수신정보 => 전송정보 초기화
			
			if(iMyDtApiTlgIn.isEmpty()) {
				rMyDtApiTlgOut.setString("세부응답코드"			, UBD_CONST.REP_CD_BAD_REQUEST_40002	 		 ); // 응답코드(40002)
				rMyDtApiTlgOut.setString("세부응답메시지"		, UBD_CONST.REP_CD_MSG_BAD_REQUEST_40002 		 ); // 응답메시지(헤더값 미존재)
				rMyDtApiTlgOut.setString("다음페이지기준개체"	, iMyDtApiTlgIn.getString("다음페이지기준개체" ));			
				
				return rMyDtApiTlgOut;			
			}	
			
		
		// =============================================================================
		// ######### ##마이데이터 API 헤더값 셋팅 
		// =============================================================================
			LData tdHeader = new LData();
			
			tdHeader = ubdCommon.get_header();
			
			sAccsTken		= tdHeader.getString("Authorization"); // 접근토큰
			sMydtTrUno		= tdHeader.getString("x-api-tran-id"); // 마이데이터거래고유번호
			sRtvlTrsYN   	= tdHeader.getString("x-api-type"	); // 정기적전송여부(1:정기적전송-"x-api-type: scheduled", 2:비정기적전송(정보주체 개입)-생략)
			sPrtlDcCd 		= tdHeader.getString("potal-dc-cd"	); // 포탈분기구분코드 (HDR:금융결제원,POR:포탈)
			sCICtt			= tdHeader.getString("ci_ctt"		); // CI내용
			sCstIdf 		= tdHeader.getString("cst_idf"		); // 고객식별자
			strDtcd         = tdHeader.getString("tran_dv_cd"); // 추출된 포탈분기구분코드
			sCstMgNo 		= "00000";							   // 고객관리번호
			sMydtUtzInsCd 	= tdHeader.getString("UTZ_INS_CD"	); // 마이데이터이용기관코드
			String cst_idf 			= tdHeader.getString("cst_idf"		); // 고객식별자
			
			LLog.debug.println( "************* [ 헤더값 ] *****************");
			LLog.debug.println( " 접근토큰 = " 				+ sAccsTken		);
			LLog.debug.println( " 거래고유번호 = " 			+ sMydtTrUno	);
			LLog.debug.println( " 정기적전송여부 = " 		+ sRtvlTrsYN	);
			LLog.debug.println( " 마이데이터이용기관코드 = "+ sMydtUtzInsCd	);
			LLog.debug.println( " 포탈분기구분코드 = "		+ sPrtlDcCd		);
			LLog.debug.println( " CI내용 = "				+ sCICtt		);
			LLog.debug.println( " 추출된 포탈분기구분코드 = "				+ strDtcd		);
		
		// =============================================================================
		// ######### ##마이데이터 API 응답헤더부 셋팅 
		// =============================================================================
		ContextUtil.setHttpResponseHeaderParam("x-api-tran-id", sMydtTrUno);	// 마이데이터거래고유번호
		
		// =============================================================================
		// ######### ##마이데이터 API 유효성검증
		// =============================================================================
		

		// 헤더 값 미존재 에러 HTTP 응답코드 : 400
		if (!UBD_CONST.PRTL_DTCD_PRTL.equals(sPrtlDcCd)) {
			if(StringUtil.trimNisEmpty(sAccsTken) || StringUtil.trimNisEmpty(sMydtTrUno)) {
				rMyDtApiTlgOut.setString("세부응답코드", UBD_CONST.REP_CD_BAD_REQUEST_40002); // 응답코드(40002)
				rMyDtApiTlgOut.setString("세부응답메시지", UBD_CONST.REP_CD_MSG_BAD_REQUEST_40002); // 응답메시지(헤더값 미존재)
				rMyDtApiTlgOut.setString("다음페이지기준개체", iMyDtApiTlgIn.getString("다음페이지기준개체"));
	
				return rMyDtApiTlgOut;
			}
		}
		/**
		 *  ※ 거래고유번호는 API요청기관에서 생성하여 API처리기관에 전송되는 값으로, API처리기관은 HTTP 응답 헤더*에 동일한 거래고유번호를
		 *     설정하여 API요청기관에 회신 * API 정상 응답뿐만 아니라 에러응답 시에도 반드시 거래고유번호를 회신
		 *  ■ 거래고유번호 : 기관코드(10자리) + 생성주체구분코드(1자리) + 부여번호(9자리)  
		 *     "M" : 마이데이터사업자 , "S" : 정보제공자 , "R" : 중계기관 , "C" : 정보수신자 , "P" : 종합포털
		 *     
		 *  ■ INPUT  : 접근토큰
		 *  ■ OUTPUT : CI
		 */
		
		iMyDtApiTlgIn.setString("CI번호_V88",sCICtt);	
		LLog.debug.println("마이데이터 국내승인내역조회 입력시작");
		
		// =============================================================================
		// ######### ##마이데이터 API 파라미터 체크 
		// =============================================================================
		Boolean bTlgFormatErr 	= false; // 전문포멧에러여부
		if (StringUtil.trimNisEmpty(sMydtUtzInsCd) && !sPrtlDcCd.equals("POR")) {
			bTlgFormatErr = true;
		} else if(StringUtil.trimNisEmpty(iMyDtApiTlgIn.getString("시작일자")) || !FormatUtil.isCharOfNum(iMyDtApiTlgIn.getString("시작일자"))) {
			bTlgFormatErr = true;
		} else if(StringUtil.trimNisEmpty(iMyDtApiTlgIn.getString("종료일자")) || !FormatUtil.isCharOfNum(iMyDtApiTlgIn.getString("종료일자"))) {
			bTlgFormatErr = true;
		}   
		if(bTlgFormatErr) {
			rMyDtApiTlgOut.setString("세부응답코드"			, UBD_CONST.REP_CD_BAD_REQUEST_40001		 	); // 응답코드(40001)
			rMyDtApiTlgOut.setString("세부응답메시지"		, UBD_CONST.REP_CD_MSG_BAD_REQUEST_40001	 	); // 응답메시지(요청파라미터 오류)
			rMyDtApiTlgOut.setString("다음페이지기준개체"	, iMyDtApiTlgIn.getString("다음페이지기준개체" ));
			
			return rMyDtApiTlgOut;			
		}		
		// =============================================================================
		// ######### ##마이데이터 API 거래고유번호 중복체크	
		// =============================================================================

		boolean bRtn = false;       //중복요청거래검증 결과 boolean 생성
		LData iDupDmd = new LData();
		
		UbdCdcSptFntCpbc cdcSptFntCpbc = new UbdCdcSptFntCpbc();
		
		// 포탈조회시 요청검증거래내역 중복 체크 검증
		if(sPrtlDcCd.equals("POR")) {
			
			iDupDmd.setString("거래발생일_V8"		, sJbStYm	  );
			iDupDmd.setString("거래고유번호_V25"	, sMydtTrUno					  ); // 거래고유번호
			iDupDmd.setString("거래구분코드_V6"		, UBD_CONST.API_DTCD_BIL_INF_INQ  ); // API구분코드
			
			bRtn = cdcSptFntCpbc.dupDmdVlnTrVln(iDupDmd);      //중복요청거래 검증 결과 수신
			
			// TODO OPEN
//			if( ! bRtn ) { //false : 중복거래 시 
//			   throw new LException(); //예외처리 유발
//			}
			LLog.debug.println( " 요청검증거래내역 = " + bRtn);
		} else {			
			
			iDupDmd.setString("거래발생일_V8"		, sJbStYm	  );
			iDupDmd.setString("거래고유번호_V25"	, sMydtTrUno					  ); // 거래고유번호
			iDupDmd.setString("거래구분코드_V6"		, UBD_CONST.API_DTCD_BIL_INF_INQ  ); // API구분코드
			
			bRtn = cdcSptFntCpbc.dupDmdTrVln(iDupDmd);      //중복요청거래 검증 결과 수신
			
			// TODO OPEN
//			if( ! bRtn ) { //false : 중복거래 시 
//			   throw new LException(); //예외처리 유발
//			}
			LLog.debug.println( " 요청거래내역 = " + bRtn);
		}
		
		// =============================================================================
		// ######### ##마이데이터 API CI 내용 가져오기 
		// =============================================================================
		LData iUsrRgInf = new LData();	// input
		LData rUsrRgInf = new LData();	// output

		String ci="";
		LData cust_info = new LData();
			
		if( 	ubdCommon.cnfmVlnTrYn() == "Y" ) { //검증거래여부 Y일시 
			ci = header_info.getString("ci_ctt"); 
			LLog.debug.println( " 검증거래여부 = " + ci);
		}else {
			cust_info = ubdCommon.select_cust_info(sAccsTken); // 포털에서 ci정보 조회
			ci = cust_info.getString("CI내용");
			//테스트를 위한 임시 조치
			ci = "qP7Jr6OWRH/FJjLkySudyM7s8381w3hDyEr9UrdlW2pPJtxYYu/ofpml77ygJO8sfSDedn0bSrw+UdGAPi5nZg=="; //있는데이터
			LLog.debug.println( " 포털에서 ci정보 조회 = " + ci);
		}
		LLog.debug.println( "  ci정보 조회 = " + ci);
		tCustInf = ubdCommon.retvCstCmn( ci );
		LLog.debug.println( " tCustInf123 = " + tCustInf);
		
		if (!UBD_CONST.PRTL_DTCD_PRTL.equals(sPrtlDcCd)) {
			if(StringUtil.trimNisEmpty(sAccsTken) || StringUtil.trimNisEmpty(sMydtTrUno)) {
				setRspReturn(UBD_CONST.REP_CD_BAD_REQUEST_40002
						   , UBD_CONST.REP_CD_MSG_BAD_REQUEST_40002 );
				return rMyDtApiTlgOut;			
			}	
			/**
			 *  ※ 거래고유번호는 API요청기관에서 생성하여 API처리기관에 전송되는 값으로, API처리기관은 HTTP 응답 헤더*에 동일한 거래고유번호를
			 *     설정하여 API요청기관에 회신 * API 정상 응답뿐만 아니라 에러응답 시에도 반드시 거래고유번호를 회신
			 *  ■ 거래고유번호 : 기관코드(10자리) + 생성주체구분코드(1자리) + 부여번호(9자리)  
			 *     "M" : 마이데이터사업자 , "S" : 정보제공자 , "R" : 중계기관 , "C" : 정보수신자 , "P" : 종합포털
			 *     
			 *  ■ INPUT  : 접근토큰
			 *  ■ OUTPUT : CI
			 */
			cust_info = ubdCommon.select_cust_info(sAccsTken);
			
			if(StringUtil.trimNisEmpty(cust_info.getString("CI내용"))) {
				setRspReturn(UBD_CONST.REP_CD_NOTFOUND_40403, UBD_CONST.REP_CD_MSG_NOTFOUND_40403);
				return rMyDtApiTlgOut;
			}
			
			sCstIdf   = cust_info.getString("고객식별자");
			sCICtt 	  = cust_info.getString("CI내용");
			sMydtClintIdiNo  = cust_info.getString("클라이언트식별번호");
			
		}else {
//			
//			if(StringUtil.trimNisEmpty(cst_idf) || StringUtil.trimNisEmpty(sCICtt) ) {
//				setRspReturn(UBD_CONST.REP_CD_NOTFOUND_40403, UBD_CONST.REP_CD_MSG_NOTFOUND_40403);
//				return rMyDtApiTlgOut;
//			}
			
			cst_idf= "";
			sCICtt = "";
			
			sCstIdf   = cst_idf;
		}
		
		// =============================================================================
		// ######### ##마이데이터 API  조회기간 : 최대60개월 체크
		// =============================================================================
		// 2.5 조회기간 : 최대12개월 체크
		String sStartMonth 	= iMyDtApiTlgIn.getString("시작일자");    
		String sEndMonth 	= iMyDtApiTlgIn.getString("종료일자");
		LLog.debug.println( " 조회시작월일 = " + sStartMonth);
		LLog.debug.println( " 조회종료월일 = " + sEndMonth);
		
		String beforSixYear = new String(); // 거래기준년월일로부터 5년전일자
		beforSixYear = DateUtil.addYear(sJbStYm,5);
		if(DateUtil.getMonthInterval (beforSixYear, sStartMonth) > 60) { //조회기간이 60개월 초과면
			
			rMyDtApiTlgOut.setString("세부응답코드"			, UBD_CONST.REP_CD_FORBIDDEN_40304 		 		 ); // 응답코드(40304)
			rMyDtApiTlgOut.setString("세부응답메시지"		, UBD_CONST.REP_CD_MSG_FORBIDDEN_40304	 		 ); // 응답메시지(최대 보존기간을 초과한 데이터 요청)
			rMyDtApiTlgOut.setString("다음페이지기준개체"	, iMyDtApiTlgIn.getString("다음페이지기준개체"	));
			
			return rMyDtApiTlgOut;
		}
		
		
		// =============================================================================
		// ######### ##마이데이터 API 페이지사이즈 : 최대500건 초과시 RETURN 
		// =============================================================================
		// 2.5 페이지사이즈 : 최대500 건 
		int iPgeSize 	= iMyDtApiTlgIn.getInt("최대조회갯수");
		
		if (iPgeSize == 0) { // 페이지사이즈가 0으로 요청되면
			setRspReturn(UBD_CONST.REP_CD_BAD_REQUEST_40001
					   , UBD_CONST.REP_CD_MSG_BAD_REQUEST_40001 );
			return rMyDtApiTlgOut;
		}
		if(iPgeSize > 500) { // 페이지 사이즈가 최대500건 초과시
			setRspReturn(UBD_CONST.REP_CD_TOO_MANY_42901
					   , UBD_CONST.REP_CD_MSG_TOO_MANY_42901 );
			return rMyDtApiTlgOut;			
		}
		
		iCrdCtgInqIn.setString(PageConstants.PGE_SIZE	, iMyDtApiTlgIn.getString("최대조회갯수"));
		iretvLstMydtPstAthHis.setString(PageConstants.PGE_SIZE	, iMyDtApiTlgIn.getString("최대조회갯수"));
		
		
		
		if (StringUtil.trimNisEmpty(iMyDtApiTlgIn.getString("다음페이지기준개체"))) {
			iretvLstMydtAthHis.setString(PageConstants.NEXT_INQ_KY, "SQ_승인년월일=1,SQ_매출승인번호=1|NK_승인년월일=,NK_매출승인번호=");
		} else {
			nextkeyme = "SQ_승인년월일=1,SQ_매출승인번호=1|NK_승인년월일=" +inputnext.substring(0,8)+ ",NK_매출승인번호=" + inputnext.substring(8);
			iretvLstMydtAthHis.setString(PageConstants.NEXT_INQ_KY, nextkeyme);
			LLog.debug.println("nextkeyme", nextkeyme);
			
		}
		

		//iMyDtApiTlgIn.setString("카드대체번호", "9490900639622298");
		
		
		
		if (strDtcd.contentEquals("CDC")) {
				
				// =============================================================================
				// ######### ##마이데이터 API 카드대체번호 => 카드기본 조회
				// =============================================================================
				
				iretvGagAltrCrdDtlByCrdAltrNo.setString("카드대체번호",  iMyDtApiTlgIn.getString("카드대체번호"));
				rretvGagAltrCrdDtlByCrdAltrNo = BizCommand.execute("com.kbcard.ubd.ebi.card.MyDtDomAthHisEbc","retvGagAltrCrdDtlByCrdAltrNo", iretvGagAltrCrdDtlByCrdAltrNo);
				LLog.debug.println("조회 결과 ");
				LLog.debug.println(rretvGagAltrCrdDtlByCrdAltrNo);
				
				// =============================================================================
				// ######### ##마이데이터 API 고객식별자 => 카드상세 조회 (해지 or 만료 카드인지 확인)
				// =============================================================================
				
				ietvGagCrdDtl.setString("카드식별자",rretvGagAltrCrdDtlByCrdAltrNo.getString("카드식별자"));
				ietvGagCrdDtl.setString("카드상세일련번호",rretvGagAltrCrdDtlByCrdAltrNo.getString("카드상세일련번호"));			
				
				try {
				retvGagCrdDtl = BizCommand.execute("com.kbcard.ubd.ebi.card.MyDtDomAthHisEbc", "retvGagCrdDtl",ietvGagCrdDtl );
				
				if ( ( retvGagCrdDtl.getString( "카드만료년월일" ).compareTo( sJbStYm ) < 0 && !( StringUtil.isEmpty( retvGagCrdDtl.getString( "카드만료년월일" ) ) ) )
						|| ( retvGagCrdDtl.getString( "해지년월일" ).compareTo( sJbStYm ) < 0 && !( StringUtil.isEmpty( retvGagCrdDtl.getString( "해지년월일" ) ) ) ) ) {
					
					// =============================================================================
					// ######### ExceptionCodeBlock ##해지 만료 카드 오류처리
					// =============================================================================
					{
						rMyDtApiTlgOut.setString("세부응답코드"			, UBD_CONST.REP_CD_NOTFOUND_40403 		 		 ); // 응답코드(40403)
						rMyDtApiTlgOut.setString("세부응답메시지"		, UBD_CONST.REP_CD_MSG_NOTFOUND_40403	 		 ); // 응답메시지(정보주체(고객) 미존재)
						rMyDtApiTlgOut.setString("다음페이지기준개체"	, iMyDtApiTlgIn.getString("다음페이지기준개체"	));
						
						return rMyDtApiTlgOut;
					}
				}
				}
				catch(LNotFoundException nf) {
					rMyDtApiTlgOut.setString("세부응답코드"			, UBD_CONST.REP_CD_NOTFOUND_40403 		 		 ); // 응답코드(40403)
					rMyDtApiTlgOut.setString("세부응답메시지"		, UBD_CONST.REP_CD_MSG_NOTFOUND_40403	 		 ); // 응답메시지(정보주체(고객) 미존재)
					rMyDtApiTlgOut.setString("다음페이지기준개체"	, iMyDtApiTlgIn.getString("다음페이지기준개체"	));
					
					return rMyDtApiTlgOut;
				}
				
				iretvLstMydtAthHis.setString("페이지사이즈_N5", iMyDtApiTlgIn.getString("최대조회갯수"));
				iretvLstMydtAthHis.setString("회원일련번호", rretvGagAltrCrdDtlByCrdAltrNo.getString("회원일련번호"));
				iretvLstMydtAthHis.setString("카드식별자", rretvGagAltrCrdDtlByCrdAltrNo.getString("카드식별자"));
				iretvLstMydtAthHis.setString("국내외구분코드", "2"); // 해외승인조회이기 때문에 2 하드코딩
				iretvLstMydtAthHis.setString("승인년월일1", iMyDtApiTlgIn.getString("시작일자"));
				iretvLstMydtAthHis.setString("승인년월일2", iMyDtApiTlgIn.getString("종료일자"));
				
				// 6개월 이전과 이후 쿼리 다르게 타게 하는거 추가
				
				sixMonthago = DateUtil.addDay(sJbStYm , -240);
				
				LLog.debug.println("6개월 기준 날짜", sixMonthago);
				
				//"6개월 기준 날짜 이후면 과거승인내역조회(실제 데이터 삭제는 240일 이후에 일어난다고 한다)
				if (TypeConvertUtil.parseTo_int(sixMonthago) <= TypeConvertUtil.parseTo_int(iMyDtApiTlgIn.getString("시작일자"))
						|| (TypeConvertUtil.parseTo_int(sixMonthago) <= TypeConvertUtil.parseTo_int(iMyDtApiTlgIn.getString("종료일자") ))) {
					LLog.debug.println("6개월 이후 과거승인내역조회");
					iretvLstMydtPstAthHis = iretvLstMydtAthHis;
					rretvLstMydtPstAthHis = (LMultiData) BizCommand.execute("com.kbcard.ubd.ebi.card.MyDtDomAthHisEbc","retvLstMydtPstAthHis", iretvLstMydtPstAthHis);
					rretvLstMydtAthHis = rretvLstMydtPstAthHis;
				}else { // 이전이면 
					LLog.debug.println("6개월 이전 승인내역조회");
					rretvLstMydtAthHis = (LMultiData) BizCommand.execute("com.kbcard.ubd.ebi.card.MyDtDomAthHisEbc","retvLstMydtAthHis", iretvLstMydtAthHis);
				
				}

				
				LLog.debug.println("조회 결과123", rretvLstMydtAthHis);
//				//rCrdCtgInqOut = (LMultiData) BizCommand.execute("com.kbcard.ubd.ebi.card.MyDtDomAthHisEbc","retvLstMydtPstAthHis", iCrdCtgInqIn);
//				
//				
//				//응답코드셋팅
//				setRspReturn(UBD_CONST.REP_CD_SUCCESS
//						   , UBD_CONST.REP_CD_MSG_SUCCESS);
//
				if (DataConvertUtil.equals(ScrollPageData.getNextYn(), "Y")) {
					inputnext2 = ScrollPageData.getNextKey();
					LLog.debug.println("inputnext2",inputnext2);
					rMyDtApiTlgOut.setString("다음페이지기준개체",( inputnext2.substring(32, 40) + inputnext2.substring(51)));
					LLog.debug.println("inputnext2",inputnext2);
				} else {
					rMyDtApiTlgOut.setString("다음페이지기준개체", "");
				}
				
				LMultiData 	tCardList 		= new LMultiData();
				String sStatus = new String();
				String sStatusNum = new String();
				String sApprStatusNum = new String();
				
				for (int anx = 0;  anx < rretvLstMydtAthHis.getDataCount(); anx++) {			
					LData tCrdCtg = new LData();
					LData iCrdDcInqIn = new LData();
					LData rCrdDcInqOut = new LData();
					
					LData tCrdCtgInqOut = rretvLstMydtAthHis.getLData(anx); // 카드목록조회출력

					LData imerchantName = new LData();  // 가맹점명 쿼리 호출 입력
					LData rmerchantName = new LData();  // 가맹점명 쿼리 호출 출력
					
					LData iApprovedAmt = new LData(); // 미화매입금액 쿼리 호출 입력
					LData rApprovedAmt = new LData(); // 미화매입금액 커리 호출 출력
	
					sStatus= tCrdCtgInqOut.getString("카드승인상태구분코드") ;
					sStatusNum= tCrdCtgInqOut.getString("전문거래구분번호") ;
					sApprStatusNum = tCrdCtgInqOut.getString("승인한도반영구분코드");

					
					tCrdCtg.setString("승인번호" 				, tCrdCtgInqOut.getString("매출승인번호"));
					tCrdCtg.setString("승인일시"				, tCrdCtgInqOut.getString("승인년월일")+ tCrdCtgInqOut.getString("매출승인시각"));
					//tCrdCtg.setString("결제상태"				, tCrdCtgInqOut.getString("카드승인상태구분코드") );
					tCrdCtg.setString("정정또는승인취소일시" 	, tCrdCtgInqOut.getString("승인취소년월일")+ tCrdCtgInqOut.getString("승인취소시각"));	
					//tCrdCtg.setString("가맹점명" 				, tCrdCtgInqOut.getString("가맹점명_V75"));
					//tCrdCtg.setInt("이용금액"				, tCrdCtgInqOut.getInt("카드승인금액")); // 수정
					tCrdCtg.setBigDecimal("정정후금액"				, tCrdCtgInqOut.getBigDecimal("정정금액"));
					tCrdCtg.setString("결제국가코드" 				, tCrdCtgInqOut.getString("ISO국가코드"));
					tCrdCtg.setString("결제시통화코드" 				, "USD");
					
					
				//	rmerchantName = BizCommand.execute("com.kbcard.ubd.ebi.card.MyDtDomAthHisEbc", "retvMerchantName", imerchantName);
				//	rretvLstMydtAthHis = (LMultiData) BizCommand.execute("com.kbcard.ubd.ebi.card.MyDtDomAthHisEbc","retvLstMydtAthHis", iretvLstMydtAthHis);
					tCrdCtg.setString("가맹점명" 				, rmerchantName.getString("해외가맹점명"));
					
					if(tCrdCtgInqOut.getString("상품중분류구분코드").equals("CP04") || tCrdCtgInqOut.getString("상품중분류구분코드").equals("CP53")) {
						tCrdCtg.setString("사용구분", "02"); // 체크
					}
					else {
						tCrdCtg.setString("사용구분", "01"); // 신용
					}
					
					if (sStatus.equals("00") || sStatus.equals("01") || sStatus.equals("02") || sStatus.equals("05") || sStatus.equals("06") ||
							sStatus.equals("08")  || sStatus.equals("15") || sStatus.equals("16")) {
						tCrdCtg.setString("결제상태"				, "01" ); // 승인
					}
					else {
						tCrdCtg.setString("결제상태"				, "02" ); // 승인취소
					}
					
					// 매출전표번호가 없으면 GBH해외이용기타부가내역을 조회
					if(tCrdCtgInqOut.getString("매출전표번호").isEmpty()) {
						try {
							imerchantName.setString("회원일련번호", tCrdCtgInqOut.getString("회원일련번호"));
							imerchantName.setString("매출승인번호", tCrdCtgInqOut.getString("매출승인번호"));
							imerchantName.setString("승인년월일",  tCrdCtgInqOut.getString("승인년월일")); 
							imerchantName.setString("매출승인시각", tCrdCtgInqOut.getString("매출승인시각"));
							
							rmerchantName = BizCommand.execute("com.kbcard.ubd.ebi.card.MyDtVovsAthHisEbc", "retvMerchantName", imerchantName);
							tCrdCtg.setString("가맹점명" 				, rmerchantName.getString("해외가맹점명"));
						}
						catch(LException e) {
						tCrdCtg.setString("가맹점명" 				, "");
					}
					}
						
						else {
							try {
								// 매출전표 번호가 있으면
							LLog.debug.println( "매출전표번호",tCrdCtgInqOut.getString("매출전표번호"));
							imerchantName.setString("해외매출전표번호", tCrdCtgInqOut.getString("매출전표번호"));
							rmerchantName = BizCommand.execute("com.kbcard.ubd.ebi.card.MyDtVovsAthHisEbc", "retvSlipMerchantName", imerchantName);
							tCrdCtg.setString("가맹점명" 				, rmerchantName.getString("가맹점명"));
							
							// bc 해외결제면
							if(rmerchantName.getString("해외매출전표번호").isEmpty()) {
								imerchantName.clear();
								imerchantName.setString("BC해외매출전표번호", tCrdCtgInqOut.getString("매출전표번호"));
								rmerchantName = BizCommand.execute("com.kbcard.ubd.ebi.card.MyDtVovsAthHisEbc", "retvBcMerchantName", imerchantName);
								tCrdCtg.setString("가맹점명" 				, rmerchantName.getString("가맹점명"));
								}
							}
						catch(devon.core.exception.LBizException be) {
							tCrdCtg.setString("가맹점명" 				, "");
						}
						
					}

				
					// 가맹점에서 이용한 금액(결제 시 통화코드에 해당하는 금액) 
					
					BigDecimal apprAmt = new BigDecimal(0);
					
					if(sStatus.equals("01") ||sStatus.equals("02")||sStatus.equals("05")) {
						if(StringUtil.substring(sStatusNum, 0 ,2).equals("01") && StringUtil.substring(sApprStatusNum, 0, 1).equals("6")){
							try {
							iApprovedAmt.setString("해외매출전표번호", "매출전표번호");
							rApprovedAmt = BizCommand.execute("com.kbcard.ubd.ebi.card.MyDtVovsAthHisEbc", "retvUsdAmt", iApprovedAmt );
							apprAmt =  rApprovedAmt.getBigDecimal("매입미화금액");
							}
							catch(LNotFoundException nf) {
								apprAmt =  tCrdCtgInqOut.getBigDecimal("해외승인미화금액");
							}
							catch(LBizException e) {
								apprAmt = new BigDecimal("0");
							}
						}
						else {
							try {
							iApprovedAmt.setString("고유매출전표번호", tCrdCtgInqOut.getString("매출전표번호"));
							iApprovedAmt.setString("회원일련번호", tCrdCtgInqOut.getString("회원일련번호"));
							rApprovedAmt = BizCommand.execute("com.kbcard.ubd.ebi.card.MyDtVovsAthHisEbc", "retvUsdAmt2", iApprovedAmt );

							apprAmt = StringUtil.decimalDelete(rApprovedAmt.getBigDecimal("해외승인미화금액"));
							
							//apprAmt = StringUtil.decimalDelete(rApprovedAmt.getBigDecimal("해외승인미화금액").multiply(new BigDecimal("100")));
							
							LLog.debug.println("이용금액" ,apprAmt);
							}
							catch(LNotFoundException nf) {
								apprAmt =  tCrdCtgInqOut.getBigDecimal("해외승인미화금액");
							}
							catch (LBizException e) {
								apprAmt = new BigDecimal("0");
							}
						}
					}
					else {
						apprAmt = tCrdCtgInqOut.getBigDecimal("해외승인미화금액");
					}
					
					tCrdCtg.setBigDecimal( "이용금액", apprAmt.divide( new BigDecimal( "100" ), 50, RoundingMode.HALF_UP ) ); //해외인 경우 //##_DqhP4KFxEeuHa83Ou-rGkw@@35
					tCardList.addLData(tCrdCtg);		
				}
				LLog.debug.println("dfdf");
				LLog.debug.println(tCardList);
				rMyDtApiTlgOut.setInt("해외승인목록_cnt"				, rretvLstMydtAthHis.getDataCount());
				rMyDtApiTlgOut.set("해외승인목록"					, tCardList);

				
				
				setRspReturn(UBD_CONST.REP_CD_SUCCESS
				   , UBD_CONST.REP_CD_MSG_SUCCESS);
				return rMyDtApiTlgOut;
				
			}else {
				
				// =============================================================================
				// ######### ##마이데이터API MCI처리계 인터페이스 호출 UBE_1_GAGS00002
				// =============================================================================
				LMultiData  rMultiMciOutPut = new LMultiData();
				LMultiData 	tCrdCtgInput 		= new LMultiData();
				LData   	iRspCdMap			= new LData(); 		// 음답코드매핑조회(input)
				LData   	tRspCdMap			= new LData(); 		// 음답코드매핑조회(output)
				
		
				try {
					iMciInput.setString("CI내용" 		, iMyDtApiTlgIn.getString("CI번호_V88"));
					iMciInput.setString("카드대체번호"  , iMyDtApiTlgIn.getString("카드대체번호"));
					iMciInput.setString("조회시작년월일"  , iMyDtApiTlgIn.getString("시작일자"));				
					iMciInput.setString("조회종료년월일"  , iMyDtApiTlgIn.getString("종료일자"));		
					
					if (StringUtil.trimNisEmpty(iMyDtApiTlgIn.getString("다음페이지기준개체"))) {
						iMciInput.setString("다음조회키_V1000", "SQ_승인년월일=1,SQ_매출승인번호=1|NK_승인년월일=,NK_매출승인번호=");
					} else {
						nextkeyme = "SQ_승인년월일=1,SQ_매출승인번호=1|NK_승인년월일=" +inputnext.substring(0,8)+ ",NK_매출승인번호=" + inputnext.substring(8);
						iMciInput.setString(PageConstants.NEXT_INQ_KY, nextkeyme);
						LLog.debug.println("nextkeyme", nextkeyme);		}
					
					iMciInput.setString("페이지사이즈_N5" 	    , iMyDtApiTlgIn.getString("최대조회갯수"));
		
					LLog.debug.println("마이데이터 해외승인내역조회 입력 :" , iMciInput);
					
					rMciInput = BizCommand.execute("com.kbcard.ubd.cpbi.cmn.MciSptFntCpbc", "retvMyDtVovsAthHis", iMciInput);
					LLog.debug.println("마이데이터 해와승인내역조회 출력 :" , rMciInput);
				}catch (LNotFoundException e) {
					setRspReturn(UBD_CONST.REP_CD_SUCCESS
							   , UBD_CONST.REP_CD_MSG_SUCCESS );
				}catch (LBizException e) {
					
					 rErrCode = e.getCode();
					 rErrMsg = ErrorCodeMessageService.getInstance().getErrorCodeMessage(rErrCode);
					
					// 응답코드매핑조회
					iRspCdMap.setString("오픈API언어구분코드"	, "KOR"		);
					iRspCdMap.setString("오픈API업무구분코드"	, "UBD"		); 
					iRspCdMap.setString("언어구분코드"			, "KOR"		);
					iRspCdMap.setString("메시지채널구분코드"	, "01"		);	// 01(단말)
					iRspCdMap.setString("오류메시지코드"		, rErrCode	);		
					iRspCdMap.setString("오류메시지출력내용"	, rErrMsg	);
					iRspCdMap.setString("처리계호출방식"		, "MCI"		); // 처리계호출방식(CDC, MCI, EAI)
					
					tRspCdMap 	= (LData) BizCommand.execute("com.kbcard.ubd.cpbi.cmn.UbdMdulSptFntCpbc", "retvRspCdMapping", iRspCdMap);  
					setRspReturn(tRspCdMap.getString("오픈API응답코드")
							   , tRspCdMap.getString("오픈API응답메시지내용") ); 
				}
				
				rMultiMciOutPut = rMciInput.getLMultiData("승인내역그리드2");
				
				for (int anx = 0; anx < rMultiMciOutPut.getDataCount(); anx++) {
					LData tCrdCtg = new LData();
					LData tCrdCtgInqOut = rMultiMciOutPut.getLData(anx);
					
					tCrdCtg.setString("승인번호" 				, tCrdCtgInqOut.getString("승인번호"));
					tCrdCtg.setString("승인일시"				, tCrdCtgInqOut.getString("승인년월일")+ tCrdCtgInqOut.getString("매출승인시각"));
					tCrdCtg.setString("결제상태"				, tCrdCtgInqOut.getString("카드승인상태구분코드") );
					tCrdCtg.setString("사용구분"				, tCrdCtgInqOut.getString("마이데이터카드구분코드"));
					tCrdCtg.setString("정정또는승인취소일시" 	, tCrdCtgInqOut.getString("승인취소년월일")+ tCrdCtgInqOut.getString("승인취소시각"));		
					tCrdCtg.setString("가맹점명" 				, tCrdCtgInqOut.getString("가맹점명_V75"));
					tCrdCtg.setBigDecimal("이용금액"				, tCrdCtgInqOut.getBigDecimal("카드승인금액_N18_3"));
					tCrdCtg.setBigDecimal("정정후금액"				, tCrdCtgInqOut.getBigDecimal("정정금액_N18_3"));
					tCrdCtg.setString("결제시통화코드" 				, tCrdCtgInqOut.getString("ISO통화코드"));
					tCrdCtg.setString("결제국가코드" 				, tCrdCtgInqOut.getString("ISO국가코드"));
					tCrdCtgInput.addLData(tCrdCtg);
					
					LLog.debug.println(tCrdCtg);
				}
				
				
				if (DataConvertUtil.equals(ScrollPageData.getNextYn(), "Y")) {
					inputnext2 = ScrollPageData.getNextKey();
					LLog.debug.println("inputnext2",inputnext2);
					rMyDtApiTlgOut.setString("다음페이지기준개체",( inputnext2.substring(32, 40) + inputnext2.substring(51)));
					LLog.debug.println("inputnext2",inputnext2);
				} else {
					rMyDtApiTlgOut.setString("다음페이지기준개체", "");
				}
				
				rMyDtApiTlgOut.setInt("해외승인목록_cnt"				, rMultiMciOutPut.getDataCount());
				rMyDtApiTlgOut.set("해외승인목록"					, tCrdCtgInput);
				
				if ("N0000000".equals(rMciInput.getString("오류메시지코드"))) {
					setRspReturn(UBD_CONST.REP_CD_SUCCESS
							   , UBD_CONST.REP_CD_MSG_SUCCESS);					
				}

				return rMyDtApiTlgOut;
			}
		}catch(LNotFoundException nfe) {
			setRspReturn(UBD_CONST.REP_CD_SUCCESS
					   , UBD_CONST.REP_CD_MSG_SUCCESS );
		}catch(LBizException lbe) {
			setRspReturn(UBD_CONST.REP_CD_SERVER_ERR_50001
					   , UBD_CONST.REP_CD_MSG_SERVER_ERR_50001);
		}catch(LException le){
			le.printStackTrace();
			setRspReturn(UBD_CONST.REP_CD_SERVER_ERR_50001
					   , UBD_CONST.REP_CD_MSG_SERVER_ERR_50001);			
		}finally {
			
			/**
			 * 마이데이터 요청내역관리/요청검증내역관리 비동기방식
			 * 1. 이용기관의 요청으로 데이터를 조회하는 경우
			 *  => 마이데이터 요청내역관리
			 * 2. KB포탈이 요청으로 데이터를 조회하는 경우
			 *  => 마이데이터 요청검증내역관리
			 */
			
			String sPrcMciInsGb = "N"; // 요청내역상세 - MCI insert 입력여부
			String sPrcEaiInsGb = "N"; // 요청내역상세 - EAI insert 입력여부
			
			LLog.debug.println(" ========== 마이데이터 요청내역관리/요청검증내역관리 비동기방식 ========= ");
			LLog.debug.println("sErrCodePrc = " + sErrCodePrc);

			if(strDtcd.equals("CDC")) {
				sdtLnkdOgtnGidNo  	= "";
				sPrcMciInsGb 		= "N"; // 요청상세내역 미입력 처리.
			} else {
				// MCI, EAI 일 경우에만 입력 처리함.
				linkResponseHeader	= (LData) ContextHandler.getContextObject(LContextKey.LINK_SYSTEM_HEADER);			
				if(!LNullUtils.isNone(linkResponseHeader)) {
					sdtLnkdOgtnGidNo 	= SystemHeaderManager.getValueFromLData(linkResponseHeader, KBCDSYSTEM_HEADERFIELD.SDT_LNKD_OGTN_GID_NO	); // 연계원거래 GUID
					
					if(!StringUtil.trimNisEmpty(sdtLnkdOgtnGidNo)) {
						sPrcMciInsGb 		= "Y"; // 요청상세내역 입력 처리.
					}
				}
			} 
			

			LData input2 	= new LData();
			LData output 	= new LData();
			LData iMciInput = new LData();
			LData rMciInput = new LData();
			LData iEaiInput = new LData();
			LData rEaiInput = new LData();
			LData lEncInf 	= new LData();
			
			sErrCode 	= rMyDtApiTlgOut.getString("세부응답코드");	
			sErrMsg 	= rMyDtApiTlgOut.getString("세부응답메시지");	

			lEncInf.setString("거래고유번호"			, sMydtTrUno					);
			lEncInf.setString("마이데이터이용기관코드"	, sMydtUtzInsCd					);
			lEncInf.setString("API구분코드"				, UBD_CONST.API_DTCD_LOANS_LTERM_INF_INQ);
			lEncInf.setString("포탈분기구분코드"		, sPrtlDcCd						);
			lEncInf.setString("처리계시스템구분"		, strDtcd						);
			lEncInf.setString("CI내용"					, sCICtt						);
			lEncInf.setString("고객식별자"				,  sCstIdf                      );
			lEncInf.setString("고객관리번호"			,        sCstMgNo);					
			lEncInf.setString("마이데이터정기전송여부"	, sRtvlTrsYN					);			
			lEncInf.setString("오픈API응답코드"			, sErrCode						);
			lEncInf.setString("오픈API응답메시지내용"	, sErrMsg						);
			lEncInf.setString("오류메시지코드"			, sErrCodePrc					);
			lEncInf.setString("오류메시지출력내용"		, sErrMsgPrc					);
			lEncInf.setString("MCI원거래GUID"			, sdtLnkdOgtnGidNo				);
			lEncInf.setString("EAI원거래GUID"			, ""							);
			lEncInf.setString("MCI인터페이스ID"			, sInnMciLinkIntfId				);
			lEncInf.setString("EAI인터페이스ID"			, ""							);
			lEncInf.setString("시스템최종갱신식별자"	, sTranId						);		
			lEncInf.setString("MCI요청상세입력여부"		, sPrcMciInsGb					);
			lEncInf.setString("EAI요청상세입력여부"		, sPrcEaiInsGb					);
			

			iMciInput = iretvLstMydtAthHis;
			rMciInput = iretvLstMydtAthHis;
//			AsyncRunner.setLogParam(input, output, iMciInput, rMciInput, iEaiInput, rEaiInput, lEncInf, "L");
			AsyncRunner.setLogParam(iMyDtApiTlgIn, rMyDtApiTlgOut, iMciInput, rMciInput, iEaiInput, rEaiInput, lEncInf);
			AsyncRunner.start();

			
			LLog.debug.println( "=======================================" );				
			LLog.debug.println( "[마이데이터API 국내승인내역조회] End ============" );
			LLog.debug.println( "=======================================" );
			
			
			
			/***
			 * 	 요청제공내용 등록정보 SET
			 */


			lEncInf.setString("인터페이스ID"				, "UBD_1_GBHS00002");
			lEncInf.setString("마이데이터이용기관코드"		, sMydtUtzInsCd);
			lEncInf.setString("거래고유번호"				, sMydtTrUno);
			if (!StringUtil.trimNisEmpty(rErrCode)) {
				lEncInf.setString("오픈API응답코드"				, rErrCode);
				lEncInf.setString("오픈API응답메시지내용"		, rErrMsg);				
			}else {
				lEncInf.setString("오픈API응답코드"				, rMciInput.getString("오류메시지코드"));
				lEncInf.setString("오픈API응답메시지내용"		, rMciInput.getString("오류메시지"));				
			}
 
	

	
		}
		return rMyDtApiTlgOut;
	}
	
	
	/**
	 * @serviceID setRspReturn
	 * @logicalName 
	 * @param LData String sErrCd, String sErrMsg 
	 */
	public void setRspReturn(String sErrCd , String sErrMsg) {
		rMyDtApiTlgOut.setString("세부응답코드"	 , sErrCd);
		rMyDtApiTlgOut.setString("세부응답메시지"	 , sErrMsg);
	}
}



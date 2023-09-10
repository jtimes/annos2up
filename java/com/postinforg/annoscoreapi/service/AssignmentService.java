package com.postinforg.annoscoreapi.service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.postinforg.annoscoreapi.mapper.AssignmentBgMapper;
import com.postinforg.annoscoreapi.mapper.AssignmentMapper;
import com.postinforg.annoscoreapi.mapper.DatasetMapper;
import com.postinforg.annoscoreapi.mapper.EvalBigMapper;
import com.postinforg.annoscoreapi.mapper.ReportMapper;
import com.postinforg.annoscoreapi.util.AssignmentUtil;
import com.postinforg.annoscoreapi.util.StrNumUtil;

import egovframework.rte.psl.dataaccess.util.EgovMap;

@Service
@Transactional(rollbackFor = Exception.class)
public class AssignmentService {
	
	@Value("${media.assignmenthome}") private String assignmentHome;
	@Value("${media.datahome}") private String annotHome;
	Logger logger = LoggerFactory.getLogger(AssignmentService.class);

	private AssignmentMapper assignmentMapper;
	private AssignmentBgMapper assignmentBgMapper;
	private AssignmentUtil assignmentUtil;
	private DatasetMapper datasetMapper;
	private ReportMapper reportMapper;
	private EvalBigMapper evalBigMapper;
	
	@Autowired
	public AssignmentService(AssignmentMapper assignmentMapper, AssignmentUtil assignmentUtil
			, AssignmentBgMapper assignmentBgMapper
			, ReportMapper reportMapper
			, DatasetMapper datasetMapper
			, EvalBigMapper evalBigMapper) {
		this.assignmentMapper = assignmentMapper;
		this.assignmentUtil = assignmentUtil;
		this.assignmentBgMapper = assignmentBgMapper;
		this.datasetMapper = datasetMapper;
		this.reportMapper = reportMapper;
		this.evalBigMapper = evalBigMapper;
	}
	
	public Map getassignmentList(Map param) {
		Map res = new HashMap();
		
		List<EgovMap> l = null;
		int total = 0;
		try {
			total = assignmentMapper.selectAssignmentTotal(param);
			l = assignmentMapper.selectAssignmentList(param);
			for(EgovMap map : l) {
				map.put("assString", assignmentUtil.getAssignmentDirFromNo(map, "no"));
			}
			param.put("list", l);
			List<EgovMap> l2 = assignmentMapper.selectAssignmentCounterList(param);
			for(EgovMap map : l) {
				for(EgovMap map2 : l2) {
					String assNo = map.get("no").toString();
					if(map2.get("assignmentNo").toString().equals(assNo)) {
						map.put("labelingTotal", map2.get("labelingTotal"));
						map.put("mgtCount", map2.get("mgtCount"));
						break;
					}
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		res.put("totalcount", total);
		res.put("list", l);
		return res;
	}

	public int assignmentDel(String no) {
		int result = 0;
		try {
			result = assignmentMapper.assignmentDel(no);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return result;
	}

	// updated table spec
	public int assignmentAdd(String name, String note, String userno, String orgcode, String location) {
		int result = 0;
		
		Map param = new HashMap();
		param.put("name", name);
		param.put("note", note);
		param.put("nameEn", userno);
		param.put("orgcode", orgcode);
		param.put("location", location);
//		param.put("usage", assignmentUtil.dirUsage(location));
		
		try {
			result = assignmentMapper.assignmentAdd(param);
			Object no = param.get("no");
			if(no != null) {
				result = Integer.parseInt(no.toString());
				String assString = assignmentUtil.getAssignmentDirFromNo(result);
				String addDir = assignmentHome + assString;
				
				assignmentUtil.makeAssignmentDirs(addDir);
				
				param.put("assignmentno", result);
				param.put("location", addDir);
				assignmentMapper.locationUpdate(param);
				
				param.put("assString", assString);
				assignmentMapper.createAssignmentTables(param);
				
				param.put("name", "eset1");
				int newDatasetNo = datasetMapper.datasetAdd(param);
				if(newDatasetNo > 0) {
					param.put("datasetno", param.get("no"));
					// datasetMapper.copyLabelingLink(param);
					reportMapper.reportAdd(param);
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return result;
	}


	public int assignmentUpdate(Map param) {
		int result = 0;
		
		try {
			result = assignmentMapper.assignmentUpdate(param);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return result;
	}

	public Map getDirList() {
		Map res = new HashMap();
		res.put("list", StrNumUtil.getDirList(assignmentHome));
		return res;
	}

	public Map getAssignmentDetail(String assignmentno) {
		Map param = new HashMap();
		param.put("assignmentno", assignmentno);
		
		Map result = null;
		
		try {
			String assString = assignmentUtil.getAssignmentDirFromNo(assignmentno);
			param.put("assString", assString);
			result = assignmentMapper.getAssignmentDetail(param);
			
			result.put("datasetReportList", datasetMapper.datasetReportList(param));
			List<Map> elist = evalBigMapper.evalClassNames(result);
			String ss = "";
			if(elist != null) 
				ss = elist.stream().map(a->a.get("name").toString()).collect(Collectors.joining(","));
			result.put("evalClassNames", ss);
		
			String addDir = assignmentHome + assString;
			result.put("defaultImagedir", addDir + AssignmentUtil.DIR_LABELING_IMG);
			result.put("defaultLabelingdir", addDir + AssignmentUtil.DIR_LABELING_JSN);
			result.put("defaultMgtdir", addDir + AssignmentUtil.DIR_ASSIGNMENT_MGT);
		}
		catch (Exception e) {
			e.printStackTrace();
		}
		return result;
	}

	public int assignmentFileAdd(Map param) {
		int result = 0;
		try {
			result = assignmentBgMapper.assignmentBgAdd(param);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return result;
	}

	public Map selectAssignmentDirList(Map param) {
		Map res = new HashMap();
		
		List<EgovMap> l = null;
		int total = 0;
		try {
			total = assignmentBgMapper.selectAssignmentDirTotal(param);
			l = assignmentBgMapper.selectAssignmentDirList(param);
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		res.put("totalcount", total);
		res.put("list", l);
		return res;
	}

	public int evalReqUpdate(Map param) {
		int res = 0;
		try {
			res = assignmentMapper.evalReqUpdate(param);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return res;
	}

	public int esetClearAll(String assignmentno) {
		int res = 0;
		EgovMap param = new EgovMap();
		param.put("assignmentno", assignmentno);
		param.put("assString", AssignmentUtil.getAssignmentDirFromNo(assignmentno));
		try {
			EgovMap ass = assignmentMapper.getAssignmentDetail(param);
			param.put("datasetno", ass.get("datasetno"));
			
			
			res = evalBigMapper.esetClearAll(param);
			
			/*
			res = evalBigMapper.deleteEvalBigFileRef(param);
			logger.debug(">>> deleteEvalBigFileRef : " + res);
			
			res = evalBigMapper.deleteEvalBigFile(param);
			logger.debug(">>> deleteEvalBigFile : " + res);
			
			param.put("predFlag", "0");
			res = evalBigMapper.evaluationObjRefClear(param);
			logger.debug(">>> evaluationObjRefClear 0 : " + res);
			
			param.put("predFlag", "1");
			res = evalBigMapper.evaluationObjRefClear(param);
			logger.debug(">>> evaluationObjRefClear 1 : " + res);
			
			res = evalBigMapper.evaluationObjClear(param);
			logger.debug(">>> evaluationObjClear : " + res);
			
			res = evalBigMapper.evaluationClsClear(param);
			logger.debug(">>> evaluationClsClear : " + res);

			// dataset_labeling clear
			res = datasetMapper.datasetLabelingClear(param);
			logger.debug(">>> datasetLabelingClear : " + res);
			
			// report clear
			res = reportMapper.deleteReportItems(param);
			logger.debug(">>> reportitems : " + res);
			res = reportMapper.deleteReports(param);
			logger.debug(">>> reports : " + res);
			
			// dataset clear
			res = datasetMapper.deleteDataset(param);
			logger.debug(">>> reportitems : " + res);
			
			 */
			res = 1;
		} catch (Exception e) {
			res = 0;
			e.printStackTrace();
		}
		
		return res;
	}

	public List assignmentIncomingList(Map param) {
		List l = null;
		try {
			l = assignmentBgMapper.assignmentIncomingList(param);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return l;
	}
	
}

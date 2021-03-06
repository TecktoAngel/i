package org.igov.service.conf;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.google.common.base.Charsets;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.util.List;
import java.util.Map;
import java.util.Date;
import java.text.SimpleDateFormat;
import org.activiti.engine.ActivitiObjectNotFoundException;
import org.activiti.engine.RuntimeService;


import org.activiti.engine.TaskService;
import org.activiti.engine.history.HistoricTaskInstance;
import org.activiti.engine.task.Attachment;
import org.activiti.engine.task.Task;
import org.igov.io.db.kv.statical.IBytesDataStorage;
import org.igov.io.db.kv.temp.IBytesDataInmemoryStorage;
import org.igov.io.db.kv.temp.exception.RecordInmemoryException;
import org.igov.io.db.kv.temp.model.ByteArrayMultipartFile;
import org.igov.model.action.vo.TaskAttachVO;
import org.igov.service.business.action.task.core.AbstractModelTask;
import static org.igov.service.business.action.task.core.AbstractModelTask.getByteArrayMultipartFileFromStorageInmemory;
import static org.igov.util.Tool.sTextTranslit;
import org.json.simple.JSONObject;
import org.igov.util.JSON.JsonRestUtils;
import org.igov.util.VariableMultipartFile;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

@Service
public class AttachmetService {
	
    @Autowired
    protected TaskService oTaskService;
        
    @Autowired
    private RuntimeService oRuntimeService;
        
    @Autowired
    private IBytesDataInmemoryStorage oBytesDataInmemoryStorage;
        
    @Autowired
    private IBytesDataStorage oBytesDataStaticStorage;
        
    private final Logger LOG = LoggerFactory.getLogger(AttachmetService.class);
	
    
    public String createAttachment (String nID_Process, String sID_Field, String sFileNameAndExt,
        	boolean bSigned, String sID_StorageType, String sContentType, List<Map<String, Object>> saAttribute_JSON,
		byte[] aContent, boolean bSetVariable) throws JsonProcessingException{
            
        LOG.info("createAttachment nID_Process: " + nID_Process);
        LOG.info("createAttachment sFileNameAndExt: " + sFileNameAndExt);
        LOG.info("createAttachment bSigned: " + bSigned);
        LOG.info("createAttachment sID_StorageType: " + sID_StorageType);
        LOG.info("createAttachment sContentType: " + sContentType);
        LOG.info("createAttachment saAttribute_JSON size: " + saAttribute_JSON.size());
        LOG.info("createAttachment aContent: " + new String(aContent));

        TaskAttachVO oTaskAttachVO = new TaskAttachVO();
            
        String sKey = null;
        
        if (aContent != null) {
            if(sID_StorageType.equals("Mongo")){
                sKey = oBytesDataStaticStorage.saveData(aContent);
            }
            if (sID_StorageType.equals("Redis")){
                try {
                    sKey = oBytesDataInmemoryStorage.putBytes(aContent);
                } catch (RecordInmemoryException ex){
                    throw new RuntimeException(ex);
                }
            }
        }else{
            throw new RuntimeException("Content is null");
        }
            
        LOG.info("database sKey: " + sKey);
            
        SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd");
        SimpleDateFormat dtf = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
           
        oTaskAttachVO.setsID_StorageType(sID_StorageType);
        oTaskAttachVO.setsKey(sKey);
        oTaskAttachVO.setsVersion(df.format(new Date()));
        oTaskAttachVO.setsDateTime(dtf.format(new Date()));
        oTaskAttachVO.setsFileNameAndExt(sFileNameAndExt);
        oTaskAttachVO.setsContentType(sContentType);
        oTaskAttachVO.setnBytes(Integer.toString(aContent.length));
        oTaskAttachVO.setbSigned(bSigned);
        oTaskAttachVO.setaAttribute(saAttribute_JSON);
        
        
        String sID_Field_Value = JsonRestUtils.toJson((Object)oTaskAttachVO);
        
        if(nID_Process != null && bSetVariable == true){
            oRuntimeService.setVariable(nID_Process, sID_Field, sID_Field_Value);
        }
	
        return sID_Field_Value;
    }
    
    public MultipartFile getAttachment(String nID_Process, String sID_Field, String sKey, String sID_StorageType) 
    //    byte[] getAttachment(String nID_Process, String sID_Field, String sKey, String sID_StorageType) 
            throws ParseException, RecordInmemoryException, IOException, ClassNotFoundException {
        MultipartFile oMultipartFile = null;
        
        byte [] aResultArray = null;
        String sFileName = null;
        String sVersion = "";
        String sContentType = "";
        ByteArrayInputStream byteArrayInputStream = null;
        
        if(nID_Process != null && sID_Field != null){
            
            Map<String, Object> variables = oRuntimeService.getVariables(nID_Process);
            
            LOG.info("VariableMap: " + variables);

            if (variables != null) {

                if (variables.containsKey(sID_Field)){
                    LOG.info("VariableMap contains found key");

                    JSONParser parser = new JSONParser();
                    JSONObject result = (JSONObject) parser.parse(String.valueOf(variables.get(sID_Field)));

                    sID_StorageType = (String)result.get("sID_StorageType");
                    sKey = (String)result.get("sKey");
                    sFileName = (String)result.get("sFileNameAndExt");
                    sVersion = (String)result.get("sVersion");
                    sContentType = (String)result.get("sContentType");      

                }
            }

            if(oMultipartFile == null){
                LOG.info("result file is null");
            }
        }
        if(sID_StorageType.equals("Mongo")){
            aResultArray = oBytesDataStaticStorage.getData(sKey);
            byteArrayInputStream = new ByteArrayInputStream(aResultArray);
            oMultipartFile = new VariableMultipartFile(byteArrayInputStream, sVersion, sFileName, sContentType);
            if (aResultArray != null) {
                LOG.info("Mongo byte array isn't null");
            }
        }
        if (sID_StorageType.equals("Redis")) {
            aResultArray = oBytesDataInmemoryStorage.getBytes(sKey);
            oMultipartFile = getByteArrayMultipartFileFromStorageInmemory(aResultArray); //приводим

            if (aResultArray != null) {
                LOG.info("Redis byte array isn't null");
            }
        }
        
        
        return oMultipartFile;
        //return aResultArray;
    }
}

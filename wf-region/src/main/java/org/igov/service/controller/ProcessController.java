/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.igov.service.controller;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import org.igov.model.analytic.access.AccessGroup;
import org.igov.model.analytic.access.AccessUser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.igov.model.analytic.process.Process;
import org.igov.model.analytic.process.ProcessTask;
import org.igov.model.analytic.attribute.Attribute;
import org.igov.model.analytic.attribute.AttributeType;
import org.igov.model.analytic.attribute.Attribute_File;
import org.igov.model.analytic.attribute.Attribute_StingShort;
import org.igov.model.analytic.process.ProcessDao;
import org.igov.model.analytic.source.SourceDB;
import org.joda.time.DateTime;
import org.springframework.beans.factory.annotation.Autowired;

/**
 *
 * @author olga
 */
@Controller
@Api(tags = {"ProcessController - процессы и задачи"})
@RequestMapping(value = "/analytic/process")
public class ProcessController {

    private static final Logger LOG = LoggerFactory.getLogger(ProcessController.class);

    private static final String JSON_TYPE = "Accept=application/json";

    @Autowired
    private ProcessDao processDao;

    @ApiOperation(value = "/setProcess", notes = "##### Process - сохранение процесса #####\n\n")
    @RequestMapping(value = "/setProcess", method = RequestMethod.POST, headers = {JSON_TYPE})
    public @ResponseBody
    Process setSubject(@RequestBody Process oProcess) {
        LOG.info("/setProcess!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!! :)");
        return creatStub();
    }

    //http://localhost:8080/wf-region/service/analytic/process/getProcess?sID_=1
    @ApiOperation(value = "/getProcess", notes = "##### Process - получение процесса #####\n\n")
    @RequestMapping(value = "/getProcess", method = RequestMethod.GET, headers = {JSON_TYPE})
    public @ResponseBody
    Process getSubject(@ApiParam(value = "внутренний ид заявки", required = true) @RequestParam(value = "sID_") String sID_,
            @ApiParam(value = "ид источника", required = false) @RequestParam(value = "nID_Source", required = false) Long nID_Source) {
        LOG.info("/getProcess!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!! :)");
        try {
            return processDao.findByIdExpected(new Long(1));
        } catch (Exception ex) {
            Process process = creatStub();
            process.setsID_(ex.getMessage());
            return process;
        }
    }

    private Process creatStub() {
        LOG.info("/setProcess!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!! :)");
        Process process = new Process();
        ProcessTask processTask = new ProcessTask();
        Attribute attribute = new Attribute();
        Attribute_StingShort attribute_StingShort = new Attribute_StingShort();
        Attribute_File attribute_File = new Attribute_File();
        AccessGroup accessGroup = new AccessGroup();
        AccessUser accessUser = new AccessUser();
        SourceDB sourceDB = new SourceDB();
        AttributeType attributeType = new AttributeType();
        //---------------------------
        process.setId(new Long(1));
        process.setoDateStart(new DateTime());
        process.setoDateFinish(new DateTime());
        process.setoSourceDB(sourceDB);
        process.setsID_("test");
        process.setsID_Data("test");

        List<ProcessTask> tasks = new ArrayList();
        tasks.add(processTask);
        process.setaProcessTask(tasks);

        List<Attribute> attributes = new ArrayList();
        attributes.add(attribute);
        process.setaAttribute(attributes);
        process.setaAttribute(attributes);
        //-------------------------------
        processTask.setId(new Long(1));
        processTask.setoDateStart(new DateTime());
        processTask.setoDateFinish(new DateTime());
        processTask.setsID_("test");
        List<AccessGroup> accessGroups = new ArrayList();
        accessGroups.add(accessGroup);
        processTask.setaAccessGroup(accessGroups);
        List<AccessUser> accessUsers = new ArrayList();
        accessUsers.add(accessUser);
        processTask.setaAccessUser(accessUsers);
        //------------------------------
        attribute.setId(new Long(1));
        attribute.setoAttributeType(attributeType);
        attribute.setoAttribute_StingShort(attribute_StingShort);
        attribute.setoAttribute_File(attribute_File);
        //------------------------------
        attribute_StingShort.setId(new Long(1));
        attribute_StingShort.setsValue("test");
        //------------------------------
        attribute_File.setId(new Long(1));
        attribute_File.setsID_Data("test");
        attribute_File.setsFileName("test");
        attribute_File.setsContentType("pdf");
        attribute_File.setsExtName("txt");
        //-------------------------------
        accessGroup.setId(new Long(1));
        accessGroup.setsID("test");
        //--------------------------------
        accessUser.setId(new Long(1));
        accessUser.setsID("test");
        //--------------------------------
        accessUser.setId(new Long(1));
        accessUser.setsID("test");
        attributeType.setId(new Long(1));
        attributeType.setName("test");

        return process;
    }

}
package com.example.myself_resume_analyzer.service.impl;

import com.alibaba.dashscope.aigc.generation.Generation;
import com.alibaba.dashscope.aigc.generation.GenerationParam;
import com.alibaba.dashscope.aigc.generation.GenerationResult;
import com.alibaba.dashscope.common.Message;
import com.alibaba.dashscope.common.Role;
import com.alibaba.dashscope.exception.InputRequiredException;
import com.alibaba.dashscope.exception.NoApiKeyException;
import com.example.myself_resume_analyzer.config.DashScopeConfig;
import com.example.myself_resume_analyzer.service.ResumeParseService;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.apache.poi.xwpf.usermodel.XWPFParagraph;
import org.springframework.stereotype.Service;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Slf4j
public class ResumeParseServiceImpl  implements ResumeParseService {
    @Resource
    private DashScopeConfig dashScopeConfig;
    @Override
    public String parseResume(byte[] resumeBytes, String fileName) {
        String text;
        if (fileName.endsWith(".pdf")){
            text  = parsePdf(resumeBytes);
        }
        else if (fileName.endsWith(".docx")){
            text = parseDocx(resumeBytes);
        }
        else {
            throw new RuntimeException("不支持的文件格式");
        }
        try {
            return  AICallOn(text);
        } catch (IOException e) {
            throw new RuntimeException(e);
        } catch (NoApiKeyException e) {
            throw new RuntimeException(e);
        } catch (InputRequiredException e) {
            throw new RuntimeException(e);
        }

    }









    // 解析PDF
    private String parsePdf(byte[] resumeBytes) {
        try (PDDocument document = Loader.loadPDF(resumeBytes)) {
            // try-with-resources：括号结束后 document 自动关闭，不用手动 close
            return new PDFTextStripper().getText(document);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }





    // 解析DOCX
    private String parseDocx(byte[] resumeBytes) {
        ByteArrayInputStream inputStream = new ByteArrayInputStream(resumeBytes);
        // try-with-resources：括号结束后 inputStream 自动关闭，不用手动 close
        //bytearrayInputStream用来创建一个字节输入流，该输入流从指定的 byte 数组中读取数据。
        try( XWPFDocument document = new XWPFDocument(inputStream)) {
            return document.getParagraphs()//获取文档中所有的段落,并转换为字符串
                  .stream()
                  .map(XWPFParagraph::getText)
                  .collect( Collectors.joining("\n"));//Collectors.joining()方法将流中的元素用指定的分隔符连接成一个字符串。
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

// 调用阿里云的API
    private String AICallOn(String resumeText) throws IOException, NoApiKeyException, InputRequiredException {
//        拼接提示词文本
        String prompt = new String(getClass()
                .getResourceAsStream("/prompts/resume_parse.txt")
                .readAllBytes(), StandardCharsets.UTF_8);
        //将提示语prompt读取为字符串，因为原本的·提示语是txt文件，需要转换为字符串
        String AllPrompt = prompt + "\n" + resumeText;//将提示语prompt和简历文本拼接起来喂给 AI

//        构造AI角色，并递交简历提取的数据给AI，返回结果
        Message systemMsg = Message.builder()
                .role(Role.SYSTEM.getValue())//设置角色为系统角色，这个是阿里云SDK的一个枚举类，里面有SYSTEM、USER、ASSISTANT、BOT、TOOL、ATTACHMENT、TOOL_CALL等角色
                .content("你是一名负责简历解析的专家")
                .build();
        Message userMsg = Message.builder()
                .role(Role.USER.getValue())
                .content(AllPrompt)
                .build();//设置用户角色，并设置用户输入的简历文本
        Generation gen = new Generation();// 创建一个 Generation 对象，用来调用阿里云的 API

        GenerationResult result = gen.call(
                GenerationParam.builder()
                        .apiKey(dashScopeConfig.getApiKey())
                        .model("qwen-plus")
                        .messages(List.of(systemMsg, userMsg))//设置系统角色和用户角色
                        //其中list of 用来创建一个列表，里面包含两个元素，一个是系统角色，一个是用户角色
                        .resultFormat(GenerationParam.ResultFormat.MESSAGE)//设置返回结果格式为消息
                        .build()
        );


//        将AI返回的json结果转换为字符串
  String AIAnswer = result.getOutput()
          .getChoices()//输出结果可能有多个，所以这里取第一个
          .get(0)
          .getMessage()//获取消息对象
          .getContent();//获取消息内容
        if (AIAnswer == null){
            throw new RuntimeException("AI解析失败"  + result.getOutput());
        }
        log.info("AI建立分析成功");
        return AIAnswer;
    }


}

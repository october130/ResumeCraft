package com.example.myself_resume_analyzer.resume.service.impl;

import com.example.myself_resume_analyzer.resume.service.ResumeParseService;
import lombok.extern.slf4j.Slf4j;
import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.apache.poi.xwpf.usermodel.XWPFParagraph;
import org.springframework.stereotype.Service;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.stream.Collectors;

@Service
@Slf4j
public class ResumeParseServiceImpl  implements ResumeParseService {

    @Override
    public String parseResume(byte[] resumeBytes, String fileName) {
        String text;
        if (fileName.endsWith(".pdf")) {
            text = parsePdf(resumeBytes);
        } else if (fileName.endsWith(".docx")) {
            text = parseDocx(resumeBytes);
        } else {
            throw new RuntimeException("不支持的文件格式");
        }
        return text;
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
        try (XWPFDocument document = new XWPFDocument(inputStream)) {
            return document.getParagraphs()//获取文档中所有的段落,并转换为字符串
                    .stream()
                    .map(XWPFParagraph::getText)
                    .collect(Collectors.joining("\n"));//Collectors.joining()方法将流中的元素用指定的分隔符连接成一个字符串。
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}


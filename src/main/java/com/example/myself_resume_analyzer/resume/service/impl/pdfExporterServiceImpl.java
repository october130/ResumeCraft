package com.example.myself_resume_analyzer.resume.service.impl;

import com.example.myself_resume_analyzer.resume.entity.ResumeEvaluation;
import com.example.myself_resume_analyzer.resume.entity.ResumeRecord;
import com.example.myself_resume_analyzer.resume.service.pdfExporterService;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.lowagie.text.*;
import com.lowagie.text.Image;
import com.lowagie.text.pdf.BaseFont;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;
import jakarta.annotation.Resource;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.SpiderWebPlot;
import org.jfree.data.category.DefaultCategoryDataset;
import org.springframework.stereotype.Service;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.List;
import java.util.Map;

@Service
public class pdfExporterServiceImpl implements pdfExporterService {

    @Resource
    private ObjectMapper objectMapper;

    @Override
    public byte[] exportPdf(Long resumeId, ResumeRecord record, ResumeEvaluation evaluation) {
        Document document = new Document(PageSize.A4);
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        PdfWriter.getInstance(document, baos);
        document.open();

        try {
            // 中文字体（从 Windows 系统目录加载宋体）
            BaseFont baseFont = BaseFont.createFont(
                    "C:/Windows/Fonts/simsun.ttc,0",
                    BaseFont.IDENTITY_H,
                    BaseFont.NOT_EMBEDDED);
            Font titleFont = new Font(baseFont, 24, Font.BOLD);
            Font normalFont = new Font(baseFont, 12);
            Font boldFont = new Font(baseFont, 14, Font.BOLD);

            // 标题
            Paragraph title = new Paragraph("AI 简历分析报告", titleFont);
            title.setAlignment(Paragraph.ALIGN_CENTER);
            document.add(title);
            document.add(Chunk.NEWLINE);

            // 基本信息
            document.add(new Paragraph("文件名：" + record.getFileName(), normalFont));
            document.add(new Paragraph("上传时间：" + record.getCreateTime(), normalFont));
            document.add(Chunk.NEWLINE);

            // 总分
            document.add(new Paragraph("总分：" + evaluation.getOverallScore() + " / 100", boldFont));
            document.add(Chunk.NEWLINE);

            // 雷达图
            byte[] chartPng = generateRadarChart(evaluation);
            Image chartImage = Image.getInstance(chartPng);
            chartImage.scaleToFit(400, 300);
            chartImage.setAlignment(Image.ALIGN_CENTER);
            document.add(chartImage);
            document.add(Chunk.NEWLINE);

            // 各维度得分表
            PdfPTable table = new PdfPTable(2);
            table.setWidthPercentage(100);
            // 表头
            table.addCell(new PdfPCell(new Paragraph("维度", boldFont)));
            table.addCell(new PdfPCell(new Paragraph("得分", boldFont)));
            // 数据
            table.addCell(new PdfPCell(new Paragraph("内容完整性", normalFont)));
            table.addCell(new PdfPCell(new Paragraph(evaluation.getContentScore() + " / 25", normalFont)));
            table.addCell(new PdfPCell(new Paragraph("结构清晰度", normalFont)));
            table.addCell(new PdfPCell(new Paragraph(evaluation.getStructureScore() + " / 20", normalFont)));
            table.addCell(new PdfPCell(new Paragraph("技能匹配度", normalFont)));
            table.addCell(new PdfPCell(new Paragraph(evaluation.getSkillMatchScore() + " / 25", normalFont)));
            table.addCell(new PdfPCell(new Paragraph("表达专业性", normalFont)));
            table.addCell(new PdfPCell(new Paragraph(evaluation.getExpressionScore() + " / 15", normalFont)));
            table.addCell(new PdfPCell(new Paragraph("项目经验", normalFont)));
            table.addCell(new PdfPCell(new Paragraph(evaluation.getProjectScore() + " / 15", normalFont)));
            document.add(table);
            document.add(Chunk.NEWLINE);

            // 优势亮点
            List<String> strengths = parseStrengths(evaluation.getStrengthsJson());
            if (!strengths.isEmpty()) {
                document.add(new Paragraph("优势亮点", boldFont));
                for (String s : strengths) {
                    document.add(new Paragraph("  •  " + s, normalFont));
                }
                document.add(Chunk.NEWLINE);
            }

            // 改进建议
            List<Map<String, String>> suggestions = parseSuggestions(evaluation.getSuggestionsJson());
            if (!suggestions.isEmpty()) {
                document.add(new Paragraph("改进建议", boldFont));
                for (Map<String, String> s : suggestions) {
                    String priority = s.getOrDefault("priority", "");
                    String recommendation = s.getOrDefault("recommendation", "");
                    document.add(new Paragraph("  [" + priority + "] " + recommendation, normalFont));
                }
                document.add(Chunk.NEWLINE);
            }

            // 综合评语
            if (evaluation.getSummary() != null && !evaluation.getSummary().isEmpty()) {
                document.add(new Paragraph("综合评语", boldFont));
                document.add(new Paragraph(evaluation.getSummary(), normalFont));
            }

        } catch (IOException e) {
            throw new RuntimeException("PDF 生成失败: " + e.getMessage(), e);
        }

        document.close();
        return baos.toByteArray();
    }

    /**
     * 解析 strengthsJson → List<String>
     */
    private List<String> parseStrengths(String json) {
        if (json == null || json.isEmpty()) return List.of();
        try {
            return objectMapper.readValue(json,
                    objectMapper.getTypeFactory().constructCollectionType(List.class, String.class));
        } catch (Exception e) {
            return List.of();
        }
    }

    /**
     * 解析 suggestionsJson → List<Map>
     */
    @SuppressWarnings("unchecked")
    private List<Map<String, String>> parseSuggestions(String json) {
        if (json == null || json.isEmpty()) return List.of();
        try {
            return objectMapper.readValue(json, List.class);
        } catch (Exception e) {
            return List.of();
        }
    }

    /**
     * 生成5维雷达图 PNG
     */
    private byte[] generateRadarChart(ResumeEvaluation evaluation) {
        DefaultCategoryDataset dataset = new DefaultCategoryDataset();
        dataset.addValue(evaluation.getContentScore()    * 100.0 / 25, "得分", "内容完整性");
        dataset.addValue(evaluation.getStructureScore()  * 100.0 / 20, "得分", "结构清晰度");
        dataset.addValue(evaluation.getSkillMatchScore() * 100.0 / 25, "得分", "技能匹配度");
        dataset.addValue(evaluation.getExpressionScore() * 100.0 / 15, "得分", "表达专业性");
        dataset.addValue(evaluation.getProjectScore()    * 100.0 / 15, "得分", "项目经验");

        SpiderWebPlot plot = new SpiderWebPlot(dataset);
        plot.setWebFilled(true);
        plot.setStartAngle(54);
        plot.setInteriorGap(0.4);

        JFreeChart chart = new JFreeChart(null, null, plot, false);
        BufferedImage image = chart.createBufferedImage(400, 300);
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        try {
            ImageIO.write(image, "PNG", baos);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return baos.toByteArray();
    }
}

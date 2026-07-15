package com.example.myself_resume_analyzer.resume.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.example.myself_resume_analyzer.common.Result.Result;
import com.example.myself_resume_analyzer.resume.service.ResumeService;
import com.example.myself_resume_analyzer.common.utils.JwTUtils;
import com.example.myself_resume_analyzer.resume.vo.ResumeDetailVO;
import com.example.myself_resume_analyzer.resume.vo.ResumeVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

@RestController
@RequestMapping("/api/resume")
@Slf4j
@Tag(name = "简历分析接口")
public class ResumeController {
    @Resource
    private ResumeService resumeService;
    @Resource
    private JwTUtils jwTUtils;
@PostMapping("/upload")
@Operation(summary = "上传简历")
    public Result<ResumeVO> upload(
            @RequestParam("file") MultipartFile file,
            HttpServletRequest  request
    ) throws IOException {

    String token = request.getHeader("Authorization").substring(7);//获取token，
    Long userId = jwTUtils.getUserIdFromToken(token);//用来通过token得到userId
    log.info("得到用户ID：{}",userId);
    return resumeService.upload(file,userId);

}
@DeleteMapping("/{id}")
@Operation(summary = "删除简历")
public Result<String> delete(@PathVariable Long id,HttpServletRequest  request){
  Long userId = (Long)request.getAttribute("userId");
    return resumeService.delete(id,userId);
}

@GetMapping("/list")
    @Operation(summary = "获取简历列表")
    public Result<IPage<ResumeVO>>list(
            @RequestParam(defaultValue = "1")
            Integer  pageNum,
            @RequestParam(defaultValue = "5")
            Integer pageSize,
            HttpServletRequest request
){
    String token = request.getHeader("Authorization").substring(7);//获取token，
    Long userId = jwTUtils.getUserIdFromToken(token);//用来通过token得到userId
    return resumeService.list(pageNum,pageSize,userId);
}

@GetMapping("/{id}")
    @Operation(summary = "获取简历详情")
  public Result<ResumeDetailVO> get (
          @PathVariable Long id,
          HttpServletRequest request
)  {
      Long userId = (Long)request.getAttribute("userId");
      return resumeService.getDetail(id,userId);
}
@PostMapping("/{id}/reanalyze")
    @Operation(summary = "重新分析简历")
    public Result<String> reanalyze(
            @PathVariable("id") Long resumeId,
            HttpServletRequest request
    ){
      Long userId = (Long)request.getAttribute("userId");
      return resumeService.reanalyze( resumeId,userId);
    }
@GetMapping("/{id}/export")
    @Operation(summary = "导出简历分析报告PDF")
    public ResponseEntity<byte[]> exportPdf(
            @PathVariable("id") Long resumeId,
            HttpServletRequest request
    ){
      Long userId = (Long)request.getAttribute("userId");
      byte[] pdfBytes =  resumeService.exportPdf(resumeId,userId);
      return ResponseEntity.ok()
              .header(HttpHeaders.CONTENT_DISPOSITION,
                      "attachment; filename*=UTF-8''"
                              + URLEncoder.encode("简历分析报告.pdf", StandardCharsets.UTF_8))
              .body(pdfBytes);//返回pdf文件,响应头中添加下载文件名，前端会自动下载
    }
}


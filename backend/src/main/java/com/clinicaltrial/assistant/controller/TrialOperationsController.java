package com.clinicaltrial.assistant.controller;
import com.clinicaltrial.assistant.service.TrialOperationsService; import lombok.RequiredArgsConstructor; import org.springframework.http.ResponseEntity; import org.springframework.web.bind.annotation.*; import java.util.Map;
@RestController @RequestMapping("/api") @RequiredArgsConstructor
public class TrialOperationsController {
 private final TrialOperationsService service;
 @GetMapping("/trials/{trialId}/operations") public ResponseEntity<Map<String,Object>> dashboard(@PathVariable Long trialId){return ResponseEntity.ok(service.dashboard(trialId));}
 @PostMapping("/participants/{id}/status") public ResponseEntity<Map<String,Object>> status(@PathVariable Long id,@RequestBody Map<String,String>b){return ResponseEntity.ok(service.updateParticipant(id,b));}
 @PostMapping("/study-visits/{id}/complete") public ResponseEntity<Map<String,Object>> complete(@PathVariable Long id,@RequestBody Map<String,String>b){return ResponseEntity.ok(service.completeVisit(id,b));}
 @PostMapping("/participants/{id}/adverse-events") public ResponseEntity<Map<String,Object>> adverse(@PathVariable Long id,@RequestBody Map<String,String>b){return ResponseEntity.ok(service.addAdverseEvent(id,b));}
}

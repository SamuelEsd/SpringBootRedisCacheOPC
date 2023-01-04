package com.elseimiu.redis_cache.controller;

import java.time.Duration;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

@RestController
@RequestMapping("/one_piece")
public class OnePieceController {

  @Autowired
  private RestTemplate restTemplate;

  @Autowired
  private StringRedisTemplate redisTemplate;

  private static final String BASE_URL = "https://api.api-onepiece.com/characters/";

  @GetMapping("/{id}")
  public ResponseEntity<?> get(@PathVariable("id") Integer id) {
    try {
      ValueOperations<String, String> valueOp = redisTemplate.opsForValue();
      if (valueOp.get(getKey(id.toString())) == null) {
        ResponseEntity<String> response = restTemplate.exchange(BASE_URL.concat(id.toString()), HttpMethod.GET,
            null,
            String.class);
        if (!response.getStatusCode().equals(HttpStatusCode.valueOf(200))) {
          return response;
        }
        valueOp.set(getKey(id.toString()), response.getBody(), Duration.ofSeconds(5));
      }

      HttpHeaders headers = new HttpHeaders();
      headers.setContentType(MediaType.APPLICATION_JSON);

      return new ResponseEntity<String>(valueOp.get(getKey(id.toString())), headers, HttpStatusCode.valueOf(200));
    } catch (Exception e) {
      return new ResponseEntity<String>(e.getMessage(), HttpStatusCode.valueOf(500));
    }
  }

  private String getKey(String id) {
    return "character:".concat(id);
  }
}

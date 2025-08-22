package com.wbozon.wb.api;

import java.util.function.Supplier;

import org.apache.hc.core5.http.HttpException;

public class RetryHandler {
   private final int maxRetries;
   private final long retryDelayMillis;

   public RetryHandler(int maxRetries, long retryDelayMillis) {
       this.maxRetries = maxRetries;
       this.retryDelayMillis = retryDelayMillis;
   }

   public <T> T executeWithRetry(Supplier<T> action) {
       for (int attempt = 1; attempt <= maxRetries; attempt++) {
           try {
               return action.get();
           } catch (HttpException e) {
               if (e.getStatusCode() == 429 && attempt < maxRetries) {
                   try {
                       Thread.sleep(retryDelayMillis);
                   } catch (InterruptedException ignored) {}
               } else {
                   throw e;
               }
           }
       }
       throw new RuntimeException("Max retries exceeded");
   }
}

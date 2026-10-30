package me.TechsCode.UltraPermissions.base.update.customRequests;

import java.net.URI;
import org.apache.http.client.methods.HttpEntityEnclosingRequestBase;

public class HttpGetWithBody extends HttpEntityEnclosingRequestBase {
   public static final String GET_METHOD = "GET";

   public HttpGetWithBody(URI var1) {
      super();
      this.setURI(var1);
   }

   public HttpGetWithBody(String var1) {
      super();
      this.setURI(URI.create(var1));
   }

   @Override
   public String getMethod() {
      return "GET";
   }
}

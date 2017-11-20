package com.example.cachuelos.utils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
 
import java.util.UUID;

import javax.activation.MimetypesFileTypeMap;
import javax.servlet.ServletException;
import javax.servlet.annotation.MultipartConfig;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.Part;
 
@WebServlet(name = "cachimages",urlPatterns = {"/cachimages/*"})
@MultipartConfig
public class Uploads extends HttpServlet {
 
 
  private static final long serialVersionUID = 2857847752169838915L;
  int BUFFER_LENGTH = 4096;
 
  protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
	 
    String secretKey = UUID.randomUUID().toString();
    String fileName="";
    
    for (Part part : request.getParts()) {
        InputStream is = request.getPart(part.getName()).getInputStream();
        fileName = getFileName(part);
       
        //FileOutputStream os = new FileOutputStream(System.getenv("OPENSHIFT_DATA_DIR") + secretKey+fileName);
        //FileOutputStream os = new FileOutputStream( "c://cachuelosimages/" + secretKey+fileName);
        FileOutputStream os = new FileOutputStream( "/opt/app-root/src/" + secretKey+fileName);
        byte[] bytes = new byte[BUFFER_LENGTH];
        int read = 0;
        while ((read = is.read(bytes, 0, BUFFER_LENGTH)) != -1) {
            os.write(bytes, 0, read);
        }
        os.flush();
        is.close();
        os.close();
        //out.println(fileName + " was uploaded to " + System.getenv("OPENSHIFT_DATA_DIR"));        
    }
	response.setContentType("application/json");
    PrintWriter out = response.getWriter();
    //out.print(Utility.constructJSON("urlImage", true, secretKey+fileName,System.getenv("OPENSHIFT_DATA_DIR") ));
    //out.print(Utility.constructJSON("urlImage", true, secretKey+fileName,"c://cachuelosimages/" ));
    out.print(Utility.constructJSON("urlImage", true, secretKey+fileName,"/opt/app-root/src/" ));
    out.flush();
  }
 
  protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
 
    String filePath = request.getRequestURI();///cachuelos/uploads/1.jpg
    //File file = new File(System.getenv("OPENSHIFT_DATA_DIR") + filePath.replace("/cachimages/",""));
    //File file = new File("c://cachuelosimages/" + filePath.replace("/cachuelos/cachimages",""));
    File file = new File("/opt/app-root/src/" + filePath.replace("/cachimages/",""));
    InputStream input = new FileInputStream(file);
 
    response.setContentLength((int) file.length());
    response.setContentType(new MimetypesFileTypeMap().getContentType(file));
 
    OutputStream output = response.getOutputStream();
    byte[] bytes = new byte[BUFFER_LENGTH];
    int read = 0;
    while ((read = input.read(bytes, 0, BUFFER_LENGTH)) != -1) {
        output.write(bytes, 0, read);
        output.flush();
    }
 
    input.close();
    output.close();
  }
 
  private String getFileName(Part part) {
        for (String cd : part.getHeader("content-disposition").split(";")) {
          if (cd.trim().startsWith("filename")) {
            return cd.substring(cd.indexOf('=') + 1).trim()
                    .replace("\"", "");
          }
        }
        return null;
      }
}
package utils

import scala.io.Source
import scala.collection.mutable.HashMap
import scala.collection.mutable.HashSet
import scala.collection.Set
import java.io.File
import org.apache.http.client.methods.HttpPost
import java.util.ArrayList
import org.apache.http.NameValuePair
import org.apache.http.message.BasicNameValuePair
import org.apache.http.client.entity.UrlEncodedFormEntity
import org.apache.http.impl.client.DefaultHttpClient
import plugins.utils.HttpUtils
import org.apache.http.HttpResponse
import org.apache.commons.io.IOUtils
import org.apache.http.auth.UsernamePasswordCredentials
import java.net.URI
import org.apache.http.entity.SerializableEntity
import java.util.List
import org.apache.http.HttpEntity

object MessageChecker extends App {

  val keyMap = new HashMap[String, HashMap[String, String]];
  val languageMap = new HashMap[String, HashMap[String, String]];  

  checkMessages
  
  sendMessages("127.0.0.1", "/content", 12345);
  //sendSingleKey(keyMap.keySet.toList.head, "127.0.0.1", "/content", 12345)
  
  def sendMessages(url:String,path:String, port:Int) {
    for(key <- keyMap.keySet) {
      sendSingleKey(key, url, path, port);
    }
  } 
  
  def sendSingleKey(key:String, url:String,path:String, port:Int) {
    var values : List[NameValuePair] = new ArrayList[NameValuePair];
    values.add(new BasicNameValuePair("key", key));
    values.add(new BasicNameValuePair("entryType", "Message"));    
    var i = 0;
    for( (lang,value) <- keyMap(key)) {      
    	values.add(new BasicNameValuePair("content[" + i + "].lang", lang));
    	values.add(new BasicNameValuePair("content[" + i + "].value", value));
    	i = i + 1;
    }    
    sendEntity(new UrlEncodedFormEntity(values), url, path, port);   
  }
  
  def sendEntity(entity: HttpEntity, url:String,path:String, port:Int) {
    val address = new URI("http", null, url, 12345, path, null, null);
    val post = new HttpPost(address);
    
    post.setEntity(entity);
    
    try {
    	val httpClient = new DefaultHttpClient;
        val r: HttpResponse = httpClient.execute(post);
    }
    catch {
      case e: Exception => println("error at sending POST request: " + e.getMessage());
    }
  }
  
  def checkMessages = {
    readMessages();  
    printExistingLanguages(languageMap.keySet);
    println();
    println("All existing keys: " + keyMap.size);
    printMissingKeys();
  }  

  def readMessages() {
    for (file <- new java.io.File("conf").listFiles()) {
      if (file.getName().matches("^messages(.([A-Za-z_]+))?$")) {
        readMessages(file);
      }
    }
  }

  def readMessages(file: File) {
    val language = getLanguage(file.getName());
    languageMap += language -> new HashMap[String, String];
    for (line <- Source.fromFile(file).getLines()) {
      analyzeLine(line, language);
    }
  }

  def getLanguage(fileName: String): String = {
    if (fileName == "messages") "default" else fileName.split('.').last
  }

  def analyzeLine(line: String, language: String) {
    if (!isComment(line)) {
      val splittedLine = line.trim().split("=");
      val key = splittedLine.head trim
      val value = splittedLine.last.trim();
      putInMap(language, key, value);
    }
  }

  def isComment(line: String): Boolean = {
    return line.trim().startsWith("#");
  }

  def putInMap(language: String, key: String, value: String) {
    if (!keyMap.contains(key)) {
      keyMap += key -> new HashMap[String, String];
    }
    keyMap(key) += language -> value;
    languageMap(language) += key -> value;
  }
  
  def printExistingLanguages(languages: Set[String]) {
    println("Existing languages: " + languages);
    for (language <- languages) {
      println(language);
    }
  }

  def printMissingKeys() {
    for (language <- languageMap.keySet) {
      printMissingKeys(language, keyMap.keySet.filter(k => !keyMap(k).contains(language)));
    }
  }

  def printMissingKeys(language: String, missingKeys: Set[String]) {
    println(String.format("Missing in %s : %s key(s)", language, missingKeys.size.toString()));
    println();
    for (key <- missingKeys) {
      println(key);
    }
    println();
  }

}

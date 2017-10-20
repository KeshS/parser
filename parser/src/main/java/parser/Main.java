/**
 * Author: Artyom Prima
 * 
 * List of things that weren't implemented:
 * Discount price property.
 * Stdout method: I have no knowledge of fetching this kind of data.
 * 
 * Everything else should work when entering a single keyword.
 */

package parser;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Scanner;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

public class Main {
	private final static String URL = "http://www.aboutyou.de/";
	//this is one big array, so one offer will contain 7 properties.
	private static ArrayList<String> offers = new ArrayList();
	
	public static void main(String args[]) throws IOException{
		@SuppressWarnings("resource")
		Scanner sc = new Scanner(System.in);  
		System.out.println("Please type in the search keyword");
		String keyword = sc.nextLine();
		Document doc;
		try{
			//searches if it found the exact keyword
			doc = Jsoup.connect(format(URL + "https://www.aboutyou.de/frauen/schuhe/%s",keyword)).get();
		}catch(Exception e){
			// If server doesn't find the keyword, does a relative search
			doc = Jsoup.connect(format(URL + "suche?term=%s&category=20201",keyword)).get();
		}
		//gets a list of all products
		Elements classNames = doc.getElementsByClass("col-xs-4 isLayout3"); 
		for(Element className : classNames){
			//link to a href
			Element link = className.select("a").first();
			//finds the name of the product
			Element linkproduct = className.select("div").get(6);
			//adds the name of the product to an array
			offers.add(linkproduct.text());
			//the other procedures repeat the same as above
			Element linkBrand = className.select("div").get(7);
			offers.add(linkBrand.text());
			Element linkArticleID = className.select("article").first();
			offers.add(linkArticleID.attr("id").replace("product-",""));
			//find the description link of the product
			String linkHref = link.attr("href");
			linkDescription(Jsoup.connect(URL + linkHref).get());
		}	
		createXMLFile();
	}

	private static void linkDescription(Document doc){
		//Searches for descriptions, shipping, and color.
		Elements linkDesc = doc.getElementsByClass("bottom-0");
		offers.add(linkDesc.text());
		Elements linkPrice = doc.getElementsByClass("js-adp-product-prices");
		Element price = linkPrice.select("span").first();
		offers.add(price.attr("data-price"));
		Elements linkShipping = doc.getElementsByClass("promise-shipping-return");
		Element shipping = linkShipping.select("li").first();
		offers.add(shipping.text());
		Elements linkColor = doc.getElementsByClass("col-xs-10 adp-selector");
		Element color = linkColor.select("a").first();
		try{
			offers.add(color.attr("title"));
		}catch(NullPointerException e){
			//if color doesn't exist then add it to null
			offers.add("null");
		}
	}
	
	private static void createXMLFile() throws IOException{
		//home directory path
		String pathHome = System.getProperty("user.home");
		//creates an empty xml file
		File xmlFile = new File(pathHome + "offers.xml");
		if (xmlFile.createNewFile()){
			System.out.println("File is created!");
		}else{
			System.out.println("File already exists.");
		}
		//tells the path of created file
		System.out.println("Path : " + xmlFile.getAbsolutePath());
		//opens Bufferedreader
	    BufferedWriter writer = new BufferedWriter(new FileWriter(xmlFile, true));
	    writer.write("<?xml version='1.0' encoding='UTF-8'?>");
	    writer.newLine();
	    writer.write("<offers>");
	    writer.newLine();
	    for(int i = 0; i < offers.size(); i++){
	    	//Since all data is in a big array, I made it iterate every 7 times and repeat the whole process creating a new offer after 7 iterations. 
	    	if((i+7)%7==0){
	    	    writer.append("<offer>");
	    	    writer.newLine();
	    	    writer.append("<name>" + offers.get(i) + "</name>");
	    	    writer.newLine();
	    	}
	    	if((i+7)%7==1){
	    		writer.append("<brand>" + offers.get(i) + "</brand>");
	    		writer.newLine();
	    	}
	    	if((i+7)%7==2){
	    		writer.append("<articleId>" + offers.get(i) + "</articleId>");
	    		writer.newLine();
	    	}
		    if((i+7)%7==3){
		    	writer.append("<description>" + offers.get(i) + "</description>");
		    	writer.newLine();
		    }
		    if((i+7)%7==4){
		    	writer.append("<price>" + offers.get(i) + "</price>");
		    	writer.newLine();
		    }
		    if((i+7)%7==5){
		    	writer.append("<shipping>" + offers.get(i) + "</shipping>");
		    	writer.newLine();
		    }
		    if((i+7)%7==6){
		    	writer.append("<color>" + offers.get(i) + "</color>");
		    	writer.newLine();
		    	writer.append("</offer>");
		    	writer.newLine();
		    }
	    }
	    writer.append("</offers>");
	    //close buffer so no leaks would happen.
	    writer.close();	
	    //end of program
	}
	//formats the URL and keyword string
	private static String format(String msg, String word){
		return String.format(msg,word);
	}
}

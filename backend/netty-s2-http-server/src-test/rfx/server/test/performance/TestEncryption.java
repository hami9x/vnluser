package rfx.server.test.performance;

import rfx.server.util.SecurityUtil;

public class TestEncryption {
    public static void main(String[] args) throws Exception {
    	
//    	//String source = "TW9NGBmeoSH6mQT6VSzuM0z45XaIUPUr1utU2JDUx58r6/VYWRC2kUz45XaIUPUrYhIrDbwDTZo=";
//    	String source =  URLDecoder.decode("i5r3W8JL%2F6k8fyyDR3CcQMpPLYWHOj1MON9LsEWKuU9S0v%2FAn8JaVIk8Mfb42hcTcrPr2gUkDoD5uHHH6FBSjA%3D%3D", "utf-8");
//    		     source = "A3poiP0H0tCKxdUniYYd/O1toGIHIZgwObLiQ 6IBqYBY82lYM1/ZtGxbPVWTtnFWSgBjrvbL/4=";
//    	String def = SecurityUtil.decryptBlowfish(source);
//    	System.out.println(def);
//    	String abc = SecurityUtil.encryptBlowfish("95fdd76dce0b930b.1368002372.1368006372.12");
//    	System.out.println(abc);
    	
//    	String[] testing = "05d059cbceead00899.1368518745.1.1368518745.1368518745".split(".");
//    	System.out.println( new Array testing)

//    	System.out.println(result);
    	
    	
    	System.out.println(SecurityUtil.decryptBeaconValue("zizgzqzjzkzhzizjzizhzozizmzmzozhzqzrzrzqzozizizgzdzizlzhzdzizkzlzdznzqzozhzn201yzqzlzgzr1yzqzjzizkzizh1y"));
    }
    //rand_aid, rand_firsttime,rand_numbervisit, rand_currenttime, rand_lasttimevisit,rand_numberidvisit,	rand_locationId
}


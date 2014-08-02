package rfx.server.util.template;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.stream.Stream;

import rfx.server.util.StringPool;
import rfx.server.util.StringUtil;
import rfx.server.util.Utils;

import com.github.jknack.handlebars.Context;
import com.github.jknack.handlebars.Handlebars;
import com.github.jknack.handlebars.Helper;
import com.github.jknack.handlebars.Options;

/**
 * @author trieu.nguyen @tantrieuf31
 *
 */
public class HandlebarsHelpers {
	static final String ParallelStream = "parallelStream";
	
	public static void register(Handlebars handlebars){		
		handlebars.registerHelper("doIf", doIfHelper);
		handlebars.registerHelper("doif", doIfHelper);
		
		handlebars.registerHelper("ifHasData", ifHasDataHelper);
		
		handlebars.registerHelper("forEach",forEachHelper);
		handlebars.registerHelper("foreach",forEachHelper);
		
		handlebars.registerHelper("eachInMap",eachInMapHelper);
		
		handlebars.registerHelper("base64Decode",base64DecodeHelper);
		handlebars.registerHelper("base64Encode",base64EncodeHelper);
		handlebars.registerHelper("randomInteger",randomIntegerHelper);
						
		handlebars.registerHelper("ifExist", ifExistHelper );
		handlebars.registerHelper("ifListHasData", ifListHasDataHelper);	
		
		handlebars.registerHelper("ifCond", ifCondHelper);	
	}
	
	public static final String OPERATOR_EQUALS = "==";    
    public static final String OPERATOR_NOT_EQUALS = "!=";
    public static final String OPERATOR_LARGER_THAN = ">";
    public static final String OPERATOR_LARGER_THAN_OR_EQUAL = ">=";
    public static final String OPERATOR_LESS_THAN = "<";
    public static final String OPERATOR_LESS_THAN_OR_EQUAL = "<=";    
    public static final String OPERATOR_NotInList = "NotInList";
    public static final String OPERATOR_InList = "InList";
        
    static boolean applyIf(Object param0, Object param1, String operator) throws IOException{
    	boolean rs = false;
    	switch (operator) {	
		    case OPERATOR_EQUALS:
		    {
		    	rs =  param0.equals(param1);
		    	break;
		    }
			case OPERATOR_NOT_EQUALS:
			{
				rs =  ! param0.equals(param1);
				break;
			}
			case OPERATOR_LARGER_THAN:
			{
				int p0 = StringUtil.safeParseInt(param0);
				int p1 = StringUtil.safeParseInt(param1);
				rs =  (p0 > p1);
				break;
			}							
			case OPERATOR_LARGER_THAN_OR_EQUAL:
			{
				int p0 = StringUtil.safeParseInt(param0);
				int p1 = StringUtil.safeParseInt(param1);
				System.out.println(p0+ " >= "+ p1);
				rs =  (p0 >= p1);
				break;
			}
			case OPERATOR_LESS_THAN:
			{
				int p0 = StringUtil.safeParseInt(param0);
				int p1 = StringUtil.safeParseInt(param1);
				rs =  (p0 < p1);
				break;
			}
			case OPERATOR_LESS_THAN_OR_EQUAL:
			{
				int p0 = StringUtil.safeParseInt(param0);
				int p1 = StringUtil.safeParseInt(param1);
				rs = (p0 <= p1);
				break;
			}
			default:
				break;
    	}
    	//System.out.println(param0  + " " + param1 + " " + operator + " rs: " + rs);    	
    	return rs;	
    }
	
	
	/**
	 * 
	 */
	static Helper<Object> ifHasDataHelper = new Helper<Object>() {
		@Override
		public CharSequence apply(Object param0, Options options)	throws IOException {				
			if(StringUtil.isNullOrEmpty(param0)){					
				return options.inverse(this);
			}
			return options.fn(this);
		}
	};
	
	
	/**
	 * 
	 */
	static Helper<Object> ifListHasDataHelper = new Helper<Object>() {
		@Override
		public CharSequence apply(Object param0, Options options)	throws IOException {				
			if(param0 != null){
				List list = (List) param0;
				return (list.size()>0 ) ? options.fn(this) : options.inverse(this);
			}
			return options.inverse(this);
		}
	};
	
	/**
	 * Sample: {{#doIf tracking '==' "yes" }} the block text {{/doIf}}
	 */
	static Helper<Object> doIfHelper = new Helper<Object>() {
		@Override
		public CharSequence apply(Object param0, Options options)	throws IOException {				
			if(param0 != null){
				Object[] toks = options.params;
				int len = toks.length;
				if(len < 2){
					if(param0.getClass().isPrimitive()){
						if(param0 instanceof Boolean){
							return Boolean.parseBoolean(param0.toString()) ? options.fn(this) : options.inverse(this);
						} else if(param0 instanceof Integer){
							return Integer.parseInt(param0.toString()) > 0 ? options.fn(this) : options.inverse(this);
						} else if(param0 instanceof Long){
							return Long.parseLong(param0.toString()) > 0l ? options.fn(this) : options.inverse(this);
						} else if(param0 instanceof Double){
							return Double.parseDouble(param0.toString()) > 0.0f ? options.fn(this) : options.inverse(this);
						}
					}					
					//the param0 != null and with no operator, just return true
					return options.fn(this);
				}
				
				String operator = options.param(0);
				Object param1 = options.param(1);				
				boolean rs = applyIf(param0, param1, operator);
				
				if(len >= 6){
					int i = 2;
					while (i < len) {														
						String logicOperator =  String.valueOf(toks[i]);
						Object p0 = toks[i+1];
						String compareOperator =  String.valueOf(toks[i+2]);
						Object p1 = toks[i+3];
						if(logicOperator.equals("&&")){
							rs = rs && applyIf(p0, p1, compareOperator);
						} else if(logicOperator.equals("||")){
							rs = rs || applyIf(p0, p1, compareOperator);
						} 
						i+=4;	
					}							
				}
				
				if(rs){
					return options.fn(this);
				}
			}
			return options.inverse(this);			
		}			
	};
	
	/**
	 * sample: {{#ifExist data }} {{data}} {{/ifExist}}
	 */
	static Helper<Object> ifExistHelper = new Helper<Object>() {
		@Override
		public CharSequence apply(Object param0, Options options)	throws IOException {				
			if(StringUtil.isNotEmpty(param0)){
				return (! param0.toString().equals("0")) ? options.fn(this) : options.inverse(this);	
			}
			return options.inverse(this);			
		}
	};
	
	/**
	 * sample: {{#ifCond websiteUrl "NotInList" "zing.vn; mp3.zing.vn; forum.zing.vn" }} the block text {{/ifCond}}
	 */
	static Helper<Object> ifCondHelper = new Helper<Object>() {
		@Override
		public CharSequence apply(Object param0, Options options)	throws IOException {				
			if(param0 != null){
				String operator = options.param(0);
				String param1 = options.param(1);
				String[] items;
				switch (operator) {			
				  	case OPERATOR_EQUALS:
						return (param0.equals(param1)) ? options.fn(this) : options.inverse(this);
					case OPERATOR_NOT_EQUALS:
						return (! param0.equals(param1)) ? options.fn(this) : options.inverse(this);
					case OPERATOR_NotInList:
					{
						items = param1.split(StringPool.SEMICOLON);
						for (String item : items) {
							if(param0.equals(item.trim())){
								return options.inverse(this);	
							}
						}
						return options.fn(this);
					}
					case OPERATOR_InList:
					{
						items = param1.split(StringPool.SEMICOLON);
						for (String item : items) {
							if(param0.equals(item.trim())){									
								return options.fn(this);
							}
						}
						return options.inverse(this);
					}						
					default:
						break;
				}	
			}
			return options.inverse(this);			
		}			
	};
	
	/**
	 * sample: {{#randomInteger}}{{/randomInteger}}
	 */
	static Helper<Object> randomIntegerHelper =  new Helper<Object>() {
		@Override
		public CharSequence apply(Object param0, Options options)	throws IOException {
			int min = 1;
			int max =  Integer.MAX_VALUE;
			if(StringUtil.isNotEmpty(param0)){
				min= StringUtil.safeParseInt(param0+"",1);
			}
			if(options.params.length == 1){
				max = options.param(0);
			}
			int r = Utils.randInt(min, max);
			return String.valueOf(r);			
		}
	};
	
	/**
	 * 
	 */
	static Helper<Object> eachInMapHelper =  new Helper<Object>() {
		@Override
		public CharSequence apply(Object param0, Options options)	throws IOException {				
			if(param0 instanceof Map){					
				@SuppressWarnings("unchecked")
				Map<String,Object> map = (Map<String, Object>) param0;
				StringBuilder out = new StringBuilder();
				map.forEach(new BiConsumer<String,Object>() {
					@Override
					public void accept(String key, Object value) {
						Map<String,Object> context = new HashMap<String, Object>(2);
						context.put(StringPool.KEY, key);
						context.put(StringPool.VALUE, value);
						try {
							String s = options.fn(context).toString();								
							out.append(s);
						} catch (Exception e) {}
						context.clear();
					}
				});
				return out.toString();
			}
			return StringPool.BLANK;
		}
	};
	
	/**
	 * sample: <br>
	 * 	{{#forEach infos "e" }}
			{{e.order}} {{e.data}} {{isNotLastItem}} 
		{{/forEach}}
	 */
	static Helper<List<Object>> forEachHelper =  new Helper<List<Object>>() {
		@Override
		public CharSequence apply(List<Object> list, Options options) throws IOException {
			if(list == null){
				return StringPool.BLANK;
			}
			int len = options.params.length;
			String itemKey = len > 0 ? options.param(0) : "item";
			boolean parallelStream = len > 1 ? options.param(1).equals(ParallelStream) : false;
			StringBuilder out = new StringBuilder();
			AtomicInteger index = new AtomicInteger(0);
			final int lastIndex = list.size() - 1;
			Stream<Object> stream;
			if(parallelStream){
				stream = list.parallelStream();
			} else {
				stream = list.stream();
			}			
			stream.forEach(new Consumer<Object>() {
				@Override
				public void accept(Object object) {
					int i = index.get();
					Context context = Context.newContext(object);
					context.data(itemKey, object);
					context.data("index", i);
					context.data("isNotLastItem", i < lastIndex);
					try {
						String s = options.fn(context).toString();								
						out.append(s);
					} catch (Exception e) {}
					finally {
						context.destroy();	
					}				
					index.incrementAndGet();
				}
			});
			return out.toString();
		}
	};
	
	
	/**
	 * Sample: {{#base64Decode "SmF2YSA4IGlzIGNvb2wgcHJvZ3JhbW1pbmcgbGFuZ3VhZ2U=" }}{{/base64Decode}} => Java 8 is cool programming language 
	 */
	static Helper<String> base64DecodeHelper =  new Helper<String>() {
		@Override
		public CharSequence apply(String s, Options options)	throws IOException {				
			if(StringUtil.isNotEmpty(s)){
				return StringUtil.base64StringDecode(s);	
			}
			return StringPool.BLANK;			
		}
	};
	
	/**
	 * Sample: {{#base64Encode "Java 8 is cool programming language" }}{{/base64Encode}} => SmF2YSA4IGlzIGNvb2wgcHJvZ3JhbW1pbmcgbGFuZ3VhZ2U= 
	 */
	static Helper<String> base64EncodeHelper =  new Helper<String>() {
		@Override
		public CharSequence apply(String s, Options options)	throws IOException {				
			if(StringUtil.isNotEmpty(s)){
				return StringUtil.base64StringEncode(s);	
			}
			return StringPool.BLANK;			
		}
	};
	
	
}

/**
 * 
 */
package uk.co.alvagem.dbview.dedupe;

import java.io.IOException;
import java.io.StringReader;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.Token;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.Term;
import org.apache.lucene.queryParser.ParseException;
import org.apache.lucene.queryParser.QueryParser;
import org.apache.lucene.search.BooleanClause;
import org.apache.lucene.search.BooleanQuery;
import org.apache.lucene.search.FuzzyQuery;
import org.apache.lucene.search.PhraseQuery;
import org.apache.lucene.search.PrefixQuery;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.TermQuery;
import org.apache.lucene.search.BooleanClause.Occur;

class QueryFactory {
	
	private Query template;
	private Analyzer analyzer;
	private Map<String,String>fields;
	
	private static Map<String,QueryHandler> handlers = new HashMap<String,QueryHandler>();
	static {
		handlers.put("TermQuery",new TermQueryHandler());
		handlers.put("BooleanQuery", new BooleanQueryHandler());
		handlers.put("PhraseQuery", new PhraseQueryHandler());
		handlers.put("PrefixQuery", new PrefixQueryHandler());
		handlers.put("FuzzyQuery", new FuzzyQueryHandler());
	}
	
	private static QueryHandler defaultHandler = new DefaultQueryHandler();
	
	public QueryFactory(Query template, Analyzer analyzer){
		this.template = template;
		this.analyzer = analyzer;
		fields = new HashMap<String,String>();
	}
	
	public QueryFactory(String query, Analyzer analyzer) throws ParseException{
		this.analyzer = analyzer;
		
		fields = new HashMap<String,String>();
		query = prepareQuery(query, fields);
		fields = invert(fields);
		
		QueryParser parser = new QueryParser("$ALL",analyzer);
		template = parser.parse(query);
		
		// Debug....
//		QueryInspector qi = new QueryInspector();
//		System.out.println("Initial Query");
//		qi.inspect(template,0);
	}
	
	/**
	 * Creates a copy of a map so that the keys and values are swapped. 
	 * @param fields
	 * @return
	 */
	private Map<String, String> invert(Map<String, String> fields) {
		Map<String,String> copy = new HashMap<String,String>();
		for(Map.Entry<String,String> entry : fields.entrySet()){
			copy.put(entry.getValue(), entry.getKey());
		}
		return copy;
	}

	public Query generateFrom(Document doc) throws IOException{
		Query query = process(template, doc);
		// Debug....
//		QueryInspector qi = new QueryInspector();
//		System.out.println("Generated Query");
//		qi.inspect(query,0);

		return query;
	}
	
	/**
	 * Determines whether field text is a substitution token.  If so, then
	 * the contents of the corresponding field in the source document should
	 * be substituted.
	 * @param text is the field text to check.
	 * @return
	 */
	public boolean isToken(String text) {
		return fields.containsKey(text);
	} 
	
	/**
	 * Given text containing a substitution token then look up the field name
	 * the token corresponds to.
	 * @param text is a substitution token to look up.
	 * @return the corresponding field name.
	 */
	public String lookupField(String text) {
		return fields.get(text);
	}

	
	private String prepareQuery(String queryText, Map<String,String>fields){
		
		StringBuffer processed = new StringBuffer();
		StringBuffer fieldName = new StringBuffer();
		
		int state = 0;
		int index = 0;
		int len = queryText.length();
		
		for(int i=0; i<len; ++i){
			char ch = queryText.charAt(i);
			
			switch (state) {
			case 0:  // normal copying of text
				if(ch == '$'){
					state = 1;
				} else {
					processed.append(ch);
				}
				break;
				
			case 1: // received a $, looking for ${
				if(ch == '{'){
					state = 2;
					fieldName.delete(0,fieldName.length());
				} else {
					processed.append('$');
					state = 0;
				}
				break;
				
			case 2: // processing a field name.
				if(ch == '}'){
					// Field name complete.
					String field = fieldName.toString();
					String subst = fields.get(field);
					if(subst == null){
						++index;
						subst = createSubstitution(index);
						fields.put(field,subst);
					}
					processed.append(subst);
					state = 0;
				} else {
					fieldName.append(ch);
				}
				break;
			}
		}
			
		return processed.toString();
	}
	
	/**
	 * @param index
	 * @return
	 */
	private String createSubstitution(int index) {
		StringBuffer acc = new StringBuffer();
		final int BASE = 26;
		do{
			int dig = index % BASE;
			acc.append((char)('a' + dig));
			index = index / BASE;
		} while (index > 0);
		
		return "zsekisp" + acc.toString() + "psikesz";  // An unlikely and unique string.
	}

	private Query process(Query query, Document doc) throws IOException{
		String name = query.getClass().getSimpleName();
		QueryHandler handler = handlers.get(name);
		if(handler == null) {
			System.out.println("Can't find Query handler for " + name);
			handler = defaultHandler;
		}
		
		Query copy = handler.process(query, this, doc);
		return copy;
	}
	
	
	private abstract static class QueryHandler {

		/**
		 * Expands a query doing field substitution as it does.  If there is no need to expand
		 * or change the query then return the original query.  You always need to copy queries
		 * with children (e.g. BooleanQuery) as the children may be expanded.
		 * @param query
		 * @param factory
		 * @param doc
		 * @return
		 * @throws IOException
		 */
		public abstract Query process(Query query, QueryFactory factory, Document doc) throws IOException;
		
		
		protected boolean shouldExpand(QueryFactory factory, Term term){
			return factory.isToken(term.text());
		}
		
		protected List<String> expand(Term term, Document doc, QueryFactory factory) throws IOException{
			String targetField = factory.lookupField(term.text()); // Field to get search text from
			String text = doc.get(targetField);
			String field = term.field();  // Field to search in.
			List<String> list = new LinkedList<String>();
			if(text != null) {
				TokenStream toks = factory.analyzer.tokenStream(field, new StringReader(text));
				Token tok;
				while( ( tok = toks.next()) != null){
					String tokText = tok.termText();
					list.add(tokText);
				}
			}
			return list;
		}
		
	}
	
	/**
	 * Default query handler for any that don't need special processing.  Just returns the
	 * source query.
	 * @author bruce.porteous
	 *
	 */
	static class DefaultQueryHandler extends QueryHandler {

		/* (non-Javadoc)
		 * @see uk.co.alvagem.dbview.QueryFactory.QueryHandler#process(org.apache.lucene.search.Query, uk.co.alvagem.dbview.QueryFactory, org.apache.lucene.document.Document)
		 */
		@Override
		public Query process(Query query, QueryFactory factory, Document doc) {
			return query;
		}
		
	}
	
	static class TermQueryHandler extends QueryHandler {

		/* (non-Javadoc)
		 * @see uk.co.alvagem.dbview.DeDupeEngine.QueryHandler#process(org.apache.lucene.search.Query, uk.co.alvagem.dbview.DeDupeEngine.QueryInspector)
		 */
		@Override
		public Query process(Query query, QueryFactory factory, Document doc) throws IOException {
			TermQuery source = (TermQuery)query;
			Query copy = null;
			Term term = source.getTerm();
			if(shouldExpand(factory, term)){
				BooleanQuery bq = new BooleanQuery();
				bq.setBoost(source.getBoost());
				String field = term.field();
				List<String> toks = expand(term,doc,factory);
				for(String tok : toks){
					bq.add(new TermQuery(new Term(field, tok)), Occur.SHOULD);
				}
				copy = bq;
			} else { 
				copy = source;
			}
			return copy;
		}
		
	} 
	
	
	static class BooleanQueryHandler extends QueryHandler {

		/* (non-Javadoc)
		 * @see uk.co.alvagem.dbview.DeDupeEngine.QueryHandler#process(org.apache.lucene.search.Query, uk.co.alvagem.dbview.DeDupeEngine.QueryInspector, int)
		 */
		@Override
		public Query process(Query query, QueryFactory factory, Document doc) throws IOException {
			BooleanQuery source = (BooleanQuery) query;
			BooleanQuery copy = new BooleanQuery();
			copy.setBoost(source.getBoost());

			BooleanClause[] clauses = source.getClauses();
			for(BooleanClause bc : clauses){
				
				Query sub = factory.process(bc.getQuery(), doc);
				BooleanClause clause = new BooleanClause(sub, bc.getOccur());
				copy.add(clause);
			}
			return copy;
		}
	} 
	
	
	static class PhraseQueryHandler extends QueryHandler {
		/* (non-Javadoc)
		 * @see uk.co.alvagem.dbview.DeDupeEngine.QueryHandler#process(org.apache.lucene.search.Query, uk.co.alvagem.dbview.DeDupeEngine.QueryInspector, int)
		 */
		@Override
		public Query process(Query query, QueryFactory factory, Document doc) throws IOException {
			PhraseQuery source = (PhraseQuery) query;
			PhraseQuery copy = source;
			
			Term[] terms = source.getTerms();
			if(terms.length == 1 && shouldExpand(factory, terms[0])){
				copy = new PhraseQuery();
				copy.setBoost(source.getBoost());
				copy.setSlop(source.getSlop());
				String field = terms[0].field();
				for(String t : expand(terms[0], doc, factory)){
					copy.add(new Term(field,t));
				}
			}
			return copy;
		} 
		
		
	} 
	
	static class PrefixQueryHandler extends QueryHandler {

		/* (non-Javadoc)
		 * @see uk.co.alvagem.dbview.DeDupeEngine.QueryHandler#process(org.apache.lucene.search.Query, uk.co.alvagem.dbview.DeDupeEngine.QueryInspector, int)
		 */
		@Override
		public Query process(Query query, QueryFactory factory, Document doc) throws IOException {
			PrefixQuery source = (PrefixQuery)query;
			Query copy = null;
			Term term = source.getPrefix();
			if(shouldExpand(factory,term)){
				String field = term.field();
				BooleanQuery bq = new BooleanQuery();
				bq.setBoost(source.getBoost());
				List<String> toks = expand(term,doc,factory);
				for(String tok : toks){
					PrefixQuery sub = new PrefixQuery(new Term(field, tok) );
					bq.add(sub, Occur.SHOULD);
				}
				copy = bq;
			} else { 
				copy = source;
			}
			return copy;
		}
		
	} 
	
	
	static class FuzzyQueryHandler extends QueryHandler {

		/* (non-Javadoc)
		 * @see uk.co.alvagem.dbview.QueryFactory.QueryHandler#process(org.apache.lucene.search.Query, uk.co.alvagem.dbview.QueryFactory, org.apache.lucene.document.Document)
		 */
		@Override
		public Query process(Query query, QueryFactory factory, Document doc) throws IOException {
			FuzzyQuery source = (FuzzyQuery)query;
			Query copy = null;
			Term term = source.getTerm();
			if(shouldExpand(factory,term)){
				String field = term.field();
				float minimumSimilarity = source.getMinSimilarity();
				int prefixLength = source.getPrefixLength();
				BooleanQuery bq = new BooleanQuery();
				bq.setBoost(source.getBoost());
				List<String> toks = expand(term,doc,factory);
				for(String tok : toks){
					FuzzyQuery sub = new FuzzyQuery(new Term(field, tok) ,minimumSimilarity, prefixLength);
					bq.add(sub, Occur.SHOULD);
				}
				copy = bq;
			} else { 
				copy = source;
			}
			return copy;
		}
		
	}



	
	
}
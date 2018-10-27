/**
 * 
 */
package uk.co.alvagem.dbview;

import java.util.HashMap;
import java.util.Map;

import org.apache.lucene.index.Term;
import org.apache.lucene.search.BooleanClause;
import org.apache.lucene.search.BooleanQuery;
import org.apache.lucene.search.MultiPhraseQuery;
import org.apache.lucene.search.MultiTermQuery;
import org.apache.lucene.search.PhraseQuery;
import org.apache.lucene.search.PrefixQuery;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.RangeQuery;
import org.apache.lucene.search.TermQuery;
import org.apache.lucene.search.spans.SpanQuery;

class QueryInspector {
	private static Map<String,QueryHandler> handlers = new HashMap<String,QueryHandler>();
	static {
		handlers.put("TermQuery",new TermQueryHandler());
		handlers.put("MultiTermQuery",new MultiTermQueryHandler());
		handlers.put("BooleanQuery", new BooleanQueryHandler());
		handlers.put("WildcardQuery", new WildcardQueryHandler());
		handlers.put("PhraseQuery", new PhraseQueryHandler());
		handlers.put("PrefixQuery", new PrefixQueryHandler());
		handlers.put("MultiPhraseQuery", new MultiPhraseQueryHandler());
		handlers.put("FuzzyQuery", new FuzzyQueryHandler());
		handlers.put("RangeQuery", new RangeQueryHandler());
		handlers.put("SpanQuery", new SpanQueryHandler());
		handlers.put("RegexQuery", new RegexQueryHandler());
	}
	
	void inspect(org.apache.lucene.search.Query query, int indent){
		
		String name = query.getClass().getSimpleName();
		QueryHandler handler = handlers.get(name);
		if(handler != null) {
			handler.process(query, this, indent);
		} else {
			System.out.println("Can't find handler for " + name);
		}
	}
	
	abstract static class QueryHandler {

		/**
		 * @param query
		 * @param queryInspector
		 */
		public abstract void process(Query query, QueryInspector queryInspector, int indent);
		
		protected String tab(int indent){
			return "                                                                   ".substring(0, indent);
		}
		
	}
	
	static class TermQueryHandler extends QueryHandler {

		/* (non-Javadoc)
		 * @see uk.co.alvagem.dbview.DeDupeEngine.QueryHandler#process(org.apache.lucene.search.Query, uk.co.alvagem.dbview.DeDupeEngine.QueryInspector)
		 */
		@Override
		public void process(Query query, QueryInspector queryInspector, int indent) {
			TermQuery tq = (TermQuery)query;
			String text = tq.getClass().getSimpleName() + ": " + tq.getTerm().field() + "->" + tq.getTerm().text();
			System.out.println(tab(indent) + text);
		}
		
	} 
	
	static class MultiTermQueryHandler extends QueryHandler {

		/* (non-Javadoc)
		 * @see uk.co.alvagem.dbview.DeDupeEngine.QueryHandler#process(org.apache.lucene.search.Query, uk.co.alvagem.dbview.DeDupeEngine.QueryInspector, int)
		 */
		@Override
		public void process(Query query, QueryInspector queryInspector,
				int indent) {
			MultiTermQuery mtq = (MultiTermQuery)query;
			String text = mtq.getClass().getSimpleName() + ": " + mtq.getTerm().field() + "->" + mtq.getTerm().text();
			System.out.println(tab(indent) + text);
		}
		
	} 
	
	static class BooleanQueryHandler extends QueryHandler {

		/* (non-Javadoc)
		 * @see uk.co.alvagem.dbview.DeDupeEngine.QueryHandler#process(org.apache.lucene.search.Query, uk.co.alvagem.dbview.DeDupeEngine.QueryInspector, int)
		 */
		@Override
		public void process(Query query, QueryInspector queryInspector,
				int indent) {
			BooleanQuery bq = (BooleanQuery) query;
			
			String text = bq.getClass().getSimpleName();
			System.out.println(tab(indent) + text);
			
			BooleanClause[] clauses = bq.getClauses();
			for(BooleanClause bc : clauses){
				if(bc.isProhibited()) {
					System.out.println(tab(indent + 2) + "Prohibited:");
				}
				if(bc.isRequired()) {
					System.out.println(tab(indent + 2) + "Required:");
				}
				queryInspector.inspect(bc.getQuery(), indent + 4);
			}
		}
	} 
	
	
	static class PhraseQueryHandler extends QueryHandler {
		/* (non-Javadoc)
		 * @see uk.co.alvagem.dbview.DeDupeEngine.QueryHandler#process(org.apache.lucene.search.Query, uk.co.alvagem.dbview.DeDupeEngine.QueryInspector, int)
		 */
		@Override
		public void process(Query query, QueryInspector queryInspector, int indent) {
			PhraseQuery pq = (PhraseQuery) query;
			
			String text = pq.getClass().getSimpleName();
			System.out.println(tab(indent) + text);
			for(Term term : pq.getTerms()){
				System.out.println(tab(indent+2) + term.field() + "->" + term.text());
			}
		} 
		
		
	} 
	
	static class PrefixQueryHandler extends QueryHandler {

		/* (non-Javadoc)
		 * @see uk.co.alvagem.dbview.DeDupeEngine.QueryHandler#process(org.apache.lucene.search.Query, uk.co.alvagem.dbview.DeDupeEngine.QueryInspector, int)
		 */
		@Override
		public void process(Query query, QueryInspector queryInspector,
				int indent) {
			PrefixQuery pq = (PrefixQuery)query;
			String text = pq.getClass().getSimpleName() + ": " + pq.getPrefix().field() + "->" + pq.getPrefix().text();
			System.out.println(tab(indent) + text);
		}
		
	} 
	
	static class MultiPhraseQueryHandler extends QueryHandler {

		/* (non-Javadoc)
		 * @see uk.co.alvagem.dbview.DeDupeEngine.QueryHandler#process(org.apache.lucene.search.Query, uk.co.alvagem.dbview.DeDupeEngine.QueryInspector, int)
		 */
		@Override
		public void process(Query query, QueryInspector queryInspector,
				int indent) {
			MultiPhraseQuery mpq = (MultiPhraseQuery)query;
			String text = mpq.getClass().getSimpleName();
			System.out.println(tab(indent) + text);
//			for(Term term : mpq.getTermArrays()){
//				System.out.println(tab(indent+2) + term.field() + "->" + term.text());
//			}

		}
		
	} 
	
	static class FuzzyQueryHandler extends MultiTermQueryHandler {} 
	static class WildcardQueryHandler extends MultiTermQueryHandler {} 
	static class RegexQueryHandler extends MultiTermQueryHandler {} 
	
	static class RangeQueryHandler extends QueryHandler {

		/* (non-Javadoc)
		 * @see uk.co.alvagem.dbview.DeDupeEngine.QueryHandler#process(org.apache.lucene.search.Query, uk.co.alvagem.dbview.DeDupeEngine.QueryInspector, int)
		 */
		@Override
		public void process(Query query, QueryInspector queryInspector,
				int indent) {
			RangeQuery rq = (RangeQuery)query;
			String text = rq.getClass().getSimpleName() + ": " 
			+ rq.getLowerTerm().field() + "->" + rq.getLowerTerm().text()
			+ " to "
			+ rq.getUpperTerm().field() + "->" + rq.getUpperTerm().text();
			System.out.println(tab(indent) + text);
			
		}
		
	} 
	
	static class SpanQueryHandler extends QueryHandler {

		/* (non-Javadoc)
		 * @see uk.co.alvagem.dbview.DeDupeEngine.QueryHandler#process(org.apache.lucene.search.Query, uk.co.alvagem.dbview.DeDupeEngine.QueryInspector, int)
		 */
		@Override
		public void process(Query query, QueryInspector queryInspector,
				int indent) {
			SpanQuery rq = (SpanQuery)query;
			String text = rq.getClass().getSimpleName() ;
			System.out.println(tab(indent) + text);
		}
		
	}
	
}
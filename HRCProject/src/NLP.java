

import java.util.List;
import java.util.Properties;

import edu.stanford.nlp.ling.*;
import edu.stanford.nlp.ling.CoreAnnotations.*;
import edu.stanford.nlp.pipeline.*;
import edu.stanford.nlp.semgraph.SemanticGraph;
import edu.stanford.nlp.semgraph.SemanticGraphCoreAnnotations.CollapsedCCProcessedDependenciesAnnotation;
import edu.stanford.nlp.trees.GrammaticalRelation;
import edu.stanford.nlp.util.CoreMap;
import edu.stanford.nlp.util.Pair;


public class NLP {

    /**
     * @param args
     */
    public static void main(String[] args) {
    	
        Properties props = new Properties();
        props.put("annotators", "tokenize, ssplit, pos, lemma, ner, parse");
 
        StanfordCoreNLP pipeline = new StanfordCoreNLP(props);

        String text = "Pick up that block"; 

        // create an empty Annotation just with the given text
        Annotation document = new Annotation(text);
        
        // run all Annotators on this text
        pipeline.annotate(document);
        
        // these are all the sentences in this document
        // a CoreMap is essentially a Map that uses class objects as keys and has values with custom types
        List<CoreMap> sentences = document.get(SentencesAnnotation.class);
  
        // traversing the words in the current sentence
        for(CoreMap sentence: sentences) {

//          // this is the Stanford dependency graph of the current sentence
          SemanticGraph dependencies = sentence.get(CollapsedCCProcessedDependenciesAnnotation.class);


          // get root of parse graph
          IndexedWord root = dependencies.getFirstRoot();
          // type of root
          String type = root.tag();
          switch (type) {
	          case "VB": processVerbPhrase(dependencies, root); break;
	          case "NN": processNounPhrase(dependencies, root); break;
	          case "DT": processDeterminer(dependencies, root); break;
	          default: System.out.println("Cannot identify sentence structure.");
          	}
        }



    }

    // Processes: {This, that} one?
    static public void processDeterminer(SemanticGraph dependencies, IndexedWord root){
        List<Pair<GrammaticalRelation,IndexedWord>> s = dependencies.childPairs(root);

        System.out.println("Identity of object: " + root.originalText().toLowerCase());
      }
    
    //Processes: {That, this, the} {block, sphere}
    static public void processNounPhrase(SemanticGraph dependencies, IndexedWord root){
      List<Pair<GrammaticalRelation,IndexedWord>> s = dependencies.childPairs(root);

      System.out.println("Identity of object: " + root.originalText().toLowerCase());
      System.out.println("Type of object: " + s.get(0).second.originalText().toLowerCase());
    }
    
    // Processes: {Pick up, put down} {that, this} {block, sphere}
    static public void processVerbPhrase(SemanticGraph dependencies, IndexedWord root){
        List<Pair<GrammaticalRelation,IndexedWord>> s = dependencies.childPairs(root);
        Pair<GrammaticalRelation,IndexedWord> prt = s.get(0);
        Pair<GrammaticalRelation,IndexedWord> dobj = s.get(1);
        
        List<Pair<GrammaticalRelation,IndexedWord>> newS = dependencies.childPairs(dobj.second);
        
        System.out.println("Action: " + root.originalText().toLowerCase() + prt.second.originalText().toLowerCase());
        System.out.println("Type of object: " + dobj.second.originalText().toLowerCase());
        System.out.println("Identity of object: " + newS.get(0).second.originalText().toLowerCase());
      }

}
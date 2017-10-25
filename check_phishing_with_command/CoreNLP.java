import java.util.LinkedList;
import java.util.List;
import java.util.Properties;

import edu.stanford.nlp.ling.CoreAnnotations.TokensAnnotation;
import edu.stanford.nlp.ling.CoreLabel;
import edu.stanford.nlp.pipeline.Annotation;
import edu.stanford.nlp.pipeline.StanfordCoreNLP;

public class CoreNLP {

    protected StanfordCoreNLP pipeline;

    public CoreNLP() {
        Properties props;
        props = new Properties();
        props.put("annotators", "tokenize, ssplit, pos, lemma");
        this.pipeline = new StanfordCoreNLP(props);
    }

    public List<String> lemmatize(String s)
    {
        List<String> lemmas = new LinkedList<String>();

        Annotation document = new Annotation(s);
        this.pipeline.annotate(document);

        for (CoreLabel token: document.get(TokensAnnotation.class)) {
            lemmas.add(token.lemma());
        }        
        return lemmas;
    }


}
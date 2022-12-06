import spacy

class NLPDoc:
    def __init__(self, text):
        self.text = text
        self.nlp_model = spacy.load("en_core_web_sm")
        self.processed_doc = self.nlp_model(text)
    
    def get_pos(self):
        pos_dict = dict()
        for token in self.processed_doc:
            pos_dict[token.text] = token.pos_
        return pos_dict
    
     

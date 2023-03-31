package uk.ac.bris.cs.scotlandyard.model;

public class MakeMove implements Move.Visitor<Boolean> {
        public Boolean visit(Move.SingleMove m) {
            return false;
        }
        public Boolean visit(Move.DoubleMove m) {
            return false;
        }
}

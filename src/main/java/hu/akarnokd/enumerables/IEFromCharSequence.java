package hu.akarnokd.enumerables;

final class IEFromCharSequence implements IEnumerable<Integer> {
    
    final CharSequence cs;
    
    IEFromCharSequence(CharSequence cs) {
        this.cs = cs;
    }
    
    @Override
    public IEnumerator<Integer> enumerator() {
        return new FromCharSequenceEnumerator(cs);
    }
    
    static final class FromCharSequenceEnumerator extends BasicEnumerator<Integer> {
        final CharSequence cs;
        
        int index;
        
        FromCharSequenceEnumerator(CharSequence cs) {
            this.cs = cs;
        }
        
        @Override
        public boolean moveNext() {
            int i = index;
            CharSequence localCs = cs;
            if (i != localCs.length()) {
                value = (int)localCs.charAt(i);
                index = i + 1;
                return true;
            }
            value = null;
            return false;
        }
    }
}

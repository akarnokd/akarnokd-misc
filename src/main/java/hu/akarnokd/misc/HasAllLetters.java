package hu.akarnokd.misc;

public class HasAllLetters {
    public static void main(String[] args)
    {
        boolean alphs[]=new boolean[26];
        int i;
        int flag=0;
        for(i=0;i<26;i++)
        {
            alphs[i]=false;
        }
        String sent="the quick brown fox jumps over the lazy dog";
        sent=sent.toUpperCase();
        sent=sent.replaceAll("\\s","");
        for(i=0;i<sent.length();i++)
        {
            char a=sent.charAt(i);
            int b=a-'A';
            alphs[b]=true;
        }
        for(i=0;i<26;i++)
        {
            if(alphs[i]==false)
            {
                flag=1;
                System.out.println("Not anagram");
                break;
            }
        }
        if(flag==0)
        {
            System.out.println("Anagram");
        }
    }
}

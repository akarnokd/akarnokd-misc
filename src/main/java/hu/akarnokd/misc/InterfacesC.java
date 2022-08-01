package hu.akarnokd.misc;

public class InterfacesC {
    interface A
    {

      void a ();

      void b ();

      void c ();

      void d ();

    }
    static abstract class B implements A
    {

      public void c ()
      {

        System.out.println ("I am c");

      }

    }
    static class M extends B
    {


      public void a ()
      {

        System.out.println ("I am a");

      }
      public void b ()
      {

        System.out.println ("I am b");

      }
      public void d ()
      {

        System.out.println ("I am d");

      }
    }

    class Abstract
    {


      public static void main (String[]args)
      {

        A a = new M ();

          a.a ();

          a.b ();

          a.c ();

          a.d ();

      }

    }
}

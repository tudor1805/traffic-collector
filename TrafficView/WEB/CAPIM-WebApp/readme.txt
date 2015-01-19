 - pt importare din SVN se foloseste File -> Import -> Checkout Maven Projects from SCM
 - pt remediere bug maven+wtp: click dr pe proiect -> Properties -> Deployment Assembly si se adauga mapare intre MAven repository si WEB-INF/lib
 e posibil sa fie necesara si stergerea + adaugarea din nou a tomcat-ului
 - si folderul src trebuie adaugat la Build Path (click dreapta ...)
 - exista o problema la construirea WAR -> se ia directorul deployat din SERVER_PATH/wtpwebapps (serverul din Eclipse)
 - pe cipsm.hpc.pub.ro mysql nu e configuerat pt a accepta conexiuni de la clienti situati pe alte statii,
 pt ca bind-address din my.cnf e setat la 127.0.0.1
 - pt ca aplicatia sa se conecteze la DB mysql tb ca in directorul Tomcat lib sa fie prezent driverul mysql
 
 C:\Documents and Settings\Narcis\workspace_jee_2\CAPIM-WebApp\WebContent
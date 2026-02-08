Modern Java 17 Reconciliation Batch

Build:
  mvn package

Run:
  java -jar target/recon-batch-1.0.0.jar <config> <payments.csv> <out_summary> <out_exceptions> <jdbc_url>

Example:
  java -jar target/recon-batch-1.0.0.jar config/recon.cfg data/payments.csv out_summary.txt out_exceptions.csv jdbc:h2:mem:recon

Test:
  mvn test

Exit Codes:
  0 = success
  1 = success with exceptions
  2 = bad args
  3 = IO or DB error
  4 = config error

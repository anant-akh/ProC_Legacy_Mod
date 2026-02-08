Legacy Pro*C Reconciliation Batch

Build:
  make

Run:
  ./bin/recon config/recon.cfg data/payments.csv out_summary.txt out_exceptions.csv user/pass@db

Exit Codes:
  0 = success
  1 = success with exceptions
  2 = bad args
  3 = IO or DB error
  4 = config error

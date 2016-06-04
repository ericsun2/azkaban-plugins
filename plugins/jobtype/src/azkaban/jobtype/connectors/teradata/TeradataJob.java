package azkaban.jobtype.connectors.teradata;

import static azkaban.jobtype.connectors.teradata.TdchConstants.*;

import java.io.File;
import java.util.List;

import org.apache.log4j.Logger;

import com.google.common.collect.ImmutableList;

import azkaban.jobtype.HadoopJavaJob;
import azkaban.jobtype.javautils.Whitelist;
import azkaban.utils.Props;

public abstract class TeradataJob extends HadoopJavaJob {
  private static final Logger logger = Logger.getLogger(TeradataJob.class);

  public TeradataJob(String jobid, Props sysProps, Props jobProps, Logger log) {
    super(jobid, sysProps, jobProps, log);
    jobProps.put(LIB_JARS_KEY, sysProps.get(LIB_JARS_KEY));
    //Initialize TDWallet if it hasn't on current JVM.
    File tempDir = new File(sysProps.getString("azkaban.temp.dir", "temp"));
    TeraDataWalletInitializer.initialize(tempDir, new File(sysProps.get(TD_WALLET_JAR)));

    if(sysProps.containsKey(Whitelist.WHITE_LIST_FILE_PATH_KEY)) {
      jobProps.put(Whitelist.WHITE_LIST_FILE_PATH_KEY, sysProps.getString(Whitelist.WHITE_LIST_FILE_PATH_KEY));
    }
  }

  @Override
  protected abstract String getJavaClass();

  /**
   * In addition to superclass's classpath, it adds jars from TDWallet unjarred folder.
   * {@inheritDoc}
   * @see azkaban.jobtype.HadoopJavaJob#getClassPaths()
   */
  @Override
  protected List<String> getClassPaths() {
    return ImmutableList.<String>builder()
                        //TDCH w. Tdwallet requires a classpath point to unjarred folder.
                        .add(TeraDataWalletInitializer.getTdchUnjarFolder())
                        .add(TeraDataWalletInitializer.getTdchUnjarFolder() + File.separator + "lib" + File.separator + "*")
                        .addAll(super.getClassPaths()).build();
  }
}

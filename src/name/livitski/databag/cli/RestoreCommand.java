/**
 *  Copyright 2010-2013 Konstantin Livitski
 *
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the Data-bag Project License.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  Data-bag Project License for more details.
 *
 *  You should find a copy of the Data-bag Project License in the
 *  `data-bag.md` file in the `LICENSE` directory
 *  of this package or repository.  If not, see
 *  <http://www.livitski.name/projects/data-bag/license>. If you have any
 *  questions or concerns, contact the project's maintainers at
 *  <http://www.livitski.name/contact>. 
 */
    
package name.livitski.databag.cli;

import java.io.File;
import java.sql.Timestamp;
import java.util.logging.Level;

import name.livitski.databag.app.Configuration;
import name.livitski.databag.app.filter.PathMatcher;
import name.livitski.databag.app.info.ReplicaInfo;
import name.livitski.databag.app.sync.RestoreService;
import name.livitski.databag.db.DBException;
import name.livitski.databag.db.Manager;

/**
 * Implements the {@link Launcher#RESTORE_COMMAND} restore command.
 */
public class RestoreCommand extends PointInTimeAbstractCommand
{
 @Override
 protected void runProtected() throws Exception
 {
  File output = getOutputFile();
  if (null == output && null == getCurrentReplica())
   throw new IllegalArgumentException(
     "The location to restore files is unknown and no replica is currently available.");
  String nameOption = getNameOption();
  if (null != nameOption && PathMatcher.hasWildcards(nameOption))
   restoreMatchingFiles(nameOption, output);
  else
   processSingleFile();
 }

 protected void restoreMatchingFiles(String patternString, File target)
 	throws Exception
 {
  blockFileAndVersionIds("restoring multiple files");
  if (null != target)
  {
   ReplicaInfo replica = getCurrentReplica();
   if (!target.isDirectory())
    throw new IllegalArgumentException("The output path '" + target
	+ "' must point to an existing directory to restore multiple files.");
   else if (null != replica)
   {
    File replicaRoot = new File(replica.getRootPath()).getCanonicalFile();
    if (target.getCanonicalFile().equals(replicaRoot))
     throw new IllegalArgumentException("The output path '" + target
	+ "' points to the current replica's root directory. Please omit the --"
	+ Launcher.SAVE_OPTION + " option to restore multiple files into the current replica."
	+ " Note the different file replacement rules that apply to in-place restore.");
   }
  }
  PathMatcher namePattern = new PathMatcher(
    patternString,
    null == target ? checkReplicasCaseSensitivity() : PathMatcher.checkFSCaseSensitivity(target)
  );
  Timestamp restorePoint = getAsOfTime();
  getRestoreService().restore(namePattern, restorePoint, target);
 }

 @Override
 protected void processKnownVersion(Number fileId, Number versionId)
   throws Exception
 {
  getRestoreService().restore(fileId, versionId, getOutputFile());
 }

 public File getOutputFile()
 {
  return outputFile;
 }

 public void setOutputFile(File outputFile)
 {
  this.outputFile = outputFile;
 }

 public RestoreCommand(Manager db, ReplicaInfo replica, Configuration config)
 {
  super(db, replica, config);
 }

 protected RestoreService getRestoreService() throws DBException
 {
  if (null == restoreService)
  {
   Configuration configuration = getConfiguration();
   Manager db = getDb();
   ReplicaInfo replica = getCurrentReplica();
   restoreService = new RestoreService(db, null == replica ? null : replica.getId(), configuration);
  }
  return restoreService;
 }

 @Override
 protected void cleanup()
 {
  super.cleanup();
  if (null != restoreService)
   try
   {
    restoreService.close();
   }
   catch (Exception e)
   {
    log().log(Level.WARNING, "Close failed for the restore service", e);
   }
 }

 private File outputFile;
 private RestoreService restoreService;
}

# Building Pipelines

## Build
 ` sh build.sh ` 
 
 
 
## Run 

 `python run.py <owner> <repository> <branch> <action>` 
 
 Python 2.7 or new is required
 
 where 
 
  * `owner` is the Github owner.
  * `repository` is the Github repository name.
  * `branch` is the branch to be build
  * `action` is one of `create`, `delete`, `update-role`
  
  For example to create a stack for  the branch `master` of the Github repository `https://github.com/BIBSYSDEV/authority-registry`
  we will run the following command:
  
  `python run.py BIBSYSDEV authority-registry master create`
  
  To delete the stack we will run :
  
  `python run.py BIBSYSDEV authority-registry master delete`
  

  
  Alternatively you can run:
  
  `java -cp build/libs/pipeline-fat.jar -Downer=<owner> -Drepository=<repository> -Dbranch=<branch>
   -Daction=<action>`
   
## Swaggerhub: 

  https://app.swaggerhub.com/apis/axthosarouris/small-api/1.0-oas3   
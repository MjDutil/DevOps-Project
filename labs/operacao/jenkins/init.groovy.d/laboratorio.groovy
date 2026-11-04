import jenkins.model.Jenkins
import hudson.security.HudsonPrivateSecurityRealm
import hudson.security.FullControlOnceLoggedInAuthorizationStrategy
import org.jenkinsci.plugins.workflow.job.WorkflowJob
import org.jenkinsci.plugins.workflow.cps.CpsFlowDefinition

def j = Jenkins.get()
def realm = new HudsonPrivateSecurityRealm(false)
realm.createAccount('b1admin', new File('/run/secrets/jenkins_password').text.trim())
j.setSecurityRealm(realm)
def auth = new FullControlOnceLoggedInAuthorizationStrategy()
auth.setAllowAnonymousRead(false)
j.setAuthorizationStrategy(auth)
j.setNumExecutors(1)
def job = j.getItem('b1-ci') ?: j.createProject(WorkflowJob, 'b1-ci')
job.setDefinition(new CpsFlowDefinition(new File('/source/Jenkinsfile').text, true))
job.save()
j.save()
println('D6: job local b1-ci criado; autenticação obrigatória, sem socket Docker.')

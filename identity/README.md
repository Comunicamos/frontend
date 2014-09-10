# Identity

This sub-project handles the Guardian profile functionality.

## Running Identity locally

Identity runs securely on a separate subdomain. As such it isn't part
of the dev-build application. The project should be run alongside
dev-build if required. The `/nginx` dir contains instructions scripts
and configuration for getting nginx setup. After following these
instructions, you'll need to run Identity on port 9009 (it doesn't run
on the default port, 9000 because that would clash with the rest of
the application). You can do this by passing the port as an argument
to the run command or by using the idrun command, which adds the port
argument for you.

So to go into the Identity project:

  `./sbt` and `project identity`

and then the following commands are equivalent:

  `idrun` or `run 9009`

The former is encouraged, in case we ever need to change the port.

So the required steps are:

* configure and run nginx (see `/nginx`)
* update your hosts file (also described in `/nginx`)
* make sure that your frontend.properties file contains

```
# ID
id.apiRoot=https://id.code.dev-guardianapis.com
id.apiClientToken=frontend-code-client-token
```

* run the Identity subproject on port 9009

With these in place, you'll be able to browse Identity on

  https://profile.thegulocal.com/

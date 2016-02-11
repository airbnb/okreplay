# &beta;etamax [![Build Status](https://travis-ci.org/cowboygneox/betamax.svg?branch=master)](https://travis-ci.org/cowboygneox/betamax)

Betamax is a tool for mocking external HTTP resources in your tests. The project was inspired by the [VCR](https://relishapp.com/vcr/vcr/docs) library for Ruby.

[The official Betamax](https://github.com/robfletcher/betamax) seems to have gone into disrepair, so the revival is in-progress.

Documentation will follow soon. Please feel free to [file an issue](https://github.com/cowboygneox/betamax/issues) if there is something you'd like to see.

### Updating the SSL Keystore

In case it's ever necessary to update the keystore (likely because the tests are failing and you're pulling your hair out), try this:

    rm betamax-tests/src/test/resources/betamax.keystore
    keytool -genkey -alias betamax -keyalg RSA -keystore betamax-tests/src/test/resources/betamax.keystore

The existing cert did not use any custom values in the certificate, but for reference, here is the dialog:

    Enter keystore password:
    Re-enter new password:
    What is your first and last name?
      [Unknown]:
    What is the name of your organizational unit?
      [Unknown]:
    What is the name of your organization?
      [Unknown]:
    What is the name of your City or Locality?
      [Unknown]:
    What is the name of your State or Province?
      [Unknown]:
    What is the two-letter country code for this unit?
      [Unknown]:
    Is CN=Unknown, OU=Unknown, O=Unknown, L=Unknown, ST=Unknown, C=Unknown correct?
      [no]:  yes
    
    Enter key password for <betamax>
      (RETURN if same as keystore password):

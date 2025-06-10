package com.chatapp.exception;

import com.chatapp.exception.BaseLocalizedException;


public class UserExceptions {

    public static class MissingUsernameException extends BaseLocalizedException {
        @Override
        public String getMessageKey() {
            return "error.missingUsername";
        }
    }

     public static class MissingEmailException extends BaseLocalizedException {
        @Override
        public String getMessageKey() {
            return "error.missingEmail";
        }
    }

    public static class MissingPasswordException extends BaseLocalizedException {
        @Override
        public String getMessageKey() {
            return "error.missingPassword";
        }
    }

    public static class UsernameTakenException extends BaseLocalizedException {
        @Override
        public String getMessageKey() {
            return "error.usernameTaken";
        }
}
    public static class EmailTakenException extends BaseLocalizedException {
        @Override
        public String getMessageKey() {
            return "error.emailTaken";
        }
    }

     public static class InvalidVerificationTokenException extends BaseLocalizedException {
        @Override
        public String getMessageKey() {
            return "error.invalidToken";
        }
    }


    public static class UserNotFoundException extends BaseLocalizedException {
        @Override
        public String getMessageKey() {
            return "user.error.notFound";
        }
    }

      public static class EmailAlreadyVerifiedException extends BaseLocalizedException {
        @Override
        public String getMessageKey() {
            return "user.error.alreadyVerified";
        }
    }



    // Add other user exceptions here similarly
}

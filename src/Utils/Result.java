package Utils;

/**
 * The purpose of this class is to mimic the Result type in Rust. Essentially, it's an alternative to throwing an Error. A method can return a Result object, which either contains an error or will tell you otherwise via isOK().
 * @author Nicholas.Sixbury
 */
public class Result<T> {
    /**
     * This enum holds whether the result holds an exception or not.
     */
    public enum ResultType {
        /**
         * This state indicates that there was no exception.
         */
        Ok,
        /**
         * This state indicates that there was an exception.
         */
        Err
    }//end enum ResultType
    
    /**
     * The exception, if one exists. Only valid if isErr() returns true.
     */
    private Exception exception = null;

    /**
     * The value, if one exists. Only valid if isOk() is true.
     */
    private T resultValue;

    /**
     * The current enum state of the object, either Ok or Err.
     */
    private ResultType state = ResultType.Ok;
    
    /**
     * If true, indicates there was no exception.
     */
    public boolean isOk() {
        return state == ResultType.Ok;
    }//end isOk()
    
    /**
     * If true, indicates there was an exception.
     */
    public boolean isErr() {
        return state == ResultType.Err;
    }//end isErr()

    /**
     * Returns the exception stored in this object.
     * @return Returns null if isErr() equals false.
     */
    public Exception getError() {
        if (isErr()) return exception;
        else return null;
    }//end getError()


    /**
     * Returns the value stored in this object.
     * @return Returns null if isOk() equals false.
     */
    public T getValue() {
        if (isOk()) return resultValue;
        else return null;
    }//end getValue()

    /**
     * Initializes the Result as not an error.
     */
    public Result() {
        state = ResultType.Ok;
        exception = null;
    }//end Ok constructor

    /**
     * Initializes the Result as Ok along with a typed value.
     * @param resultValue The value to send along as a result.
     */
    public Result(T resultValue) {
        state = ResultType.Ok;
        exception = null;
        this.resultValue = resultValue;
    }//end value constructor

    /**
     * Initializes the Result as an error.
     * @param e The exception that we're calling an error
     */
    public Result(Exception e) {
        state = ResultType.Err;
        exception = e;
    }//end Err constructor
}//end Result class

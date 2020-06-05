/** File containing constants used throughout project. */
public final class Constants {
  /* Entity Properties: */

  /** The property representing the text of a comment on the webpage. */
  public static final String COMMENT_TEXT = "text";

  /** The property representing the name of the author of a comment. */
  public static final String COMMENT_AUTHOR = "name";

  /** The property representing the timestamp when the comment was posted (in 
    * milliseconds since the epoch). */
  public static final String COMMENT_TIMESTAMP = "time";

  /* Request Parameters for DataServlet POST: */

  /** The text of a submitted comment. */
  public static final String INPUTTED_TEXT = "text-input";

  /** The author of a submitted comment. */
  public static final String INPUTTED_AUTHOR_NAME = "author";

  /* Request Parameters for DataServlet GET: */

  /** The user's request to flip to the next page, previous page, or stay on 
    * the same page.  */
  public static final String PAGE_ACTION = "pageAction";

  /** The user's search, querying the text contents and author names in the 
    * comment section. */
  public static final String SEARCH_QUERY = "search";

  /** The index of the first comment currently being displayed on the page. */
  public static final String PAGE_TOKEN = "pageToken";

  /** The user's selection of how many comments to display per page. */
  public static final String NUMBER_PER_PAGE = "numberToDisplay";

  /* Request Parameters for DeleteServlet: */

  /** Which comment should be deleted from the database, either "all" or the id of a specific comment. */
  public static final String WHICH_COMMENT_TO_DELETE = "whichData";
}

<?php
$name = $_POST['form-contact-name'];
$email = $_POST['form-contact-email'];
$message = $_POST['form-contact-message'];

$to = 'domain@example.com';
$subject = 'Message From Zoner';

$body = "";
$body .= "Name: ";
$body .= $name;
$body .= "\n\n";

$body .= "";
$body .= "Message: ";
$body .= $message;
$body .= "\n";

$headers = 'From: ' .$email . "\r\n";

//$headers = 'From: noreply@domain.com' . "\r\n";

//$body .= "";
//$body .= "Email: ";
//$body .= $email;
//$body .= "\n";

if (filter_var($email, FILTER_VALIDATE_EMAIL)) {
mail($to, $subject, $body, $headers);
echo '<span id="valid">Your Email was sent!</span>';
}else{
echo '<span id="invalid">Your message cannot be sent.</span>';
}

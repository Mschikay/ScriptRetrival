package main

import (
	"bytes"
	"crypto/tls"
	"flag"
	"fmt"
	"golang.org/x/net/html"
	"io"
	"log"
	"net/http"
	"net/url"
	"os"
	"regexp"
	"strconv"
	"strings"
)

func usage() {
	//fmt.Fprintf(os.Stderr, "usage: crawl https://www.imsdb.com/all%20scripts/")
	flag.PrintDefaults()
	os.Exit(2)
}

func main() {
	args := "https://www.imsdb.com/all%20scripts/"
	fmt.Println("get all enque links....")
	links := enqueue(args)
	contents := []string{}
	fmt.Println("content....")
	for _, link := range links {
		re, _ := regexp.Compile(`Scripts/(.*) Script`)
		match := re.FindStringSubmatch(link)[0]
		match = strings.Replace(match, "Scripts/", "", 1)
		match = strings.Replace(match, " Script", "", 1)
		match = strings.Replace(match, ", The", "", -1)
		fmt.Println(match)
		//content := getScript(link)
		//contents = append(contents, content)
		contents = append(contents, match)
	}
	//fmt.Println("write...")
	appendFile(contents)
}


func checkFile(e error) {
	if e != nil {
		panic(e)
	}
}


func appendFile(contents []string){
	// If the file doesn't exist, create it, or append to the file
	f, err := os.OpenFile("title", os.O_APPEND|os.O_CREATE|os.O_WRONLY, 0777)
	if err != nil {
		log.Fatal(err)
	}
	for id, content := range contents {
		//fmt.Println(id)
		if content != ""{
			if _, err := f.Write([]byte(strconv.Itoa(id) + "\n" + content + "\n")); err != nil {
				log.Fatal(err)
			}
		}
	}

	if err := f.Close(); err != nil {
		log.Fatal(err)
	}
}

func getScript(uri string) string {
	transport := &http.Transport{
		TLSClientConfig: &tls.Config{
			InsecureSkipVerify: true,
		},
	}
	client := http.Client{Transport: transport}
	resp, err := client.Get(uri)
	if err != nil {
		return ""
	}
	defer resp.Body.Close()

	scriptLink := getScriptLink(resp.Body)
	if scriptLink == ""{
		return ""
	}
	//fmt.Println(scriptLink)
	content := getContent(scriptLink)
	return content
}

func getContent(uri string) string {
	transport := &http.Transport{
		TLSClientConfig: &tls.Config{
			InsecureSkipVerify: true,
		},
	}
	client := http.Client{Transport: transport}
	resp, err := client.Get(uri)
	if err != nil {
		return ""
	}
	defer resp.Body.Close()
	httpBody := resp.Body
	buf := new(bytes.Buffer)
	buf.ReadFrom(httpBody)
	s := buf.String()
	re, _ := regexp.Compile(`(?s)<td class="scrtext">(.*)<table width="85%`)
	matched := re.FindString(s)
	//fmt.Println(strTrimSpace)
	//fmt.Println("matched", matched)
	return matched
}

func getScriptLink(httpBody io.Reader) string {
	page := html.NewTokenizer(httpBody)
	for {
		tokenType := page.Next()
		if tokenType == html.ErrorToken {
			return ""
		}
		token := page.Token()
		if tokenType == html.StartTagToken && token.DataAtom.String() == "a" {
			for _, attr := range token.Attr {
				if attr.Key == "href" {
					tl := trimHash(attr.Val)
					matched, _ := regexp.MatchString(`^(?i)/scripts/(.*)html$`, tl)
					if matched == true {
						uri := "https://www.imsdb.com" + tl
						return uri
					}
				}
			}
		}
	}
	return ""
}

func filterQueue(in chan string, out chan string) {
	var seen = make(map[string]bool)
	for val := range in {
		if !seen[val] {
			seen[val] = true
			out <- val
		}
	}
}

func enqueue(uri string) [] string {
	transport := &http.Transport{
		TLSClientConfig: &tls.Config{
			InsecureSkipVerify: true,
		},
	}
	client := http.Client{Transport: transport}
	resp, err := client.Get(uri)
	if err != nil {
		return []string{}
	}
	defer resp.Body.Close()

	links := getLinks(resp.Body)
	return links
}

func getLinks(httpBody io.Reader) []string {
	links := []string{}
	page := html.NewTokenizer(httpBody)
	for {
		tokenType := page.Next()
		if tokenType == html.ErrorToken {
			return links
		}
		token := page.Token()
		if tokenType == html.StartTagToken && token.DataAtom.String() == "a" {
			for _, attr := range token.Attr {
				if attr.Key == "href" {
					tl := "https://www.imsdb.com" + trimHash(attr.Val)
					matched, _ := regexp.MatchString(`^https://(.*)Script.html$`, tl)
					if matched == true {
						links = append(links, tl)
					}
				}
			}
		}
	}
	return links
}

func resolv(sl *[]string, ml []string) {
	for _, str := range ml {
		if check(*sl, str) == false {
			*sl = append(*sl, str)
		}
	}
}

// check looks to see if a url exits in the slice.
func check(sl []string, s string) bool {
	var check bool
	for _, str := range sl {
		if str == s {
			check = true
			break
		}
	}
	return check
}

// trimHash slices a hash # from the link
func trimHash(l string) string {
	if strings.Contains(l, "#") {
		var index int
		for n, str := range l {
			if strconv.QuoteRune(str) == "'#'" {
				index = n
				break
			}
		}
		return l[:index]
	}
	return l
}

func fixUrl(href, base string) string {
	uri, err := url.Parse(href)
	if err != nil {
		return ""
	}
	baseUrl, err := url.Parse(base)
	if err != nil {
		return ""
	}
	uri = baseUrl.ResolveReference(uri)
	return uri.String()
}
